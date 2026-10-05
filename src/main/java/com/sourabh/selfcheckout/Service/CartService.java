package com.sourabh.selfcheckout.Service;

import com.sourabh.selfcheckout.Dto.CartItemResponse;
import com.sourabh.selfcheckout.Dto.CartResponse;
import com.sourabh.selfcheckout.Dto.CheckoutResponse;
import com.sourabh.selfcheckout.Entity.Cart;
import com.sourabh.selfcheckout.Entity.CartItem;
import com.sourabh.selfcheckout.Entity.Customer;
import com.sourabh.selfcheckout.Entity.Inventory;
import com.sourabh.selfcheckout.Entity.Order;
import com.sourabh.selfcheckout.Entity.OrderItem;
import com.sourabh.selfcheckout.Entity.Payment;
import com.sourabh.selfcheckout.Entity.Product;
import com.sourabh.selfcheckout.Enum.CartStatus;
import com.sourabh.selfcheckout.Exception.OutOfStockException;
import com.sourabh.selfcheckout.Exception.ProductNotFoundException;
import com.sourabh.selfcheckout.Repo.CartItemRepository;
import com.sourabh.selfcheckout.Repo.CartRepository;
import com.sourabh.selfcheckout.Repo.CustomerRepository;
import com.sourabh.selfcheckout.Repo.InventoryRepository;
import com.sourabh.selfcheckout.Repo.OrderItemRepository;
import com.sourabh.selfcheckout.Repo.OrderRepository;
import com.sourabh.selfcheckout.Repo.PaymentRepository;
import com.sourabh.selfcheckout.Repo.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class CartService {

    private final ProductRepository productRepo;
    private final CartRepository cartRepo;
    private final CartItemRepository cartItemRepo;
    private final InventoryRepository inventoryRepo;
    private final CustomerRepository customerRepo;
    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final PaymentRepository paymentRepo;
    private final PaymentProcessor paymentProcessor;

    public CartService(ProductRepository p, CartRepository c, CartItemRepository ci,
                       InventoryRepository inventoryRepo, CustomerRepository customerRepo,
                       OrderRepository orderRepo, OrderItemRepository orderItemRepo,
                       PaymentRepository paymentRepo, PaymentProcessor paymentProcessor) {
        this.productRepo = p;
        this.cartRepo = c;
        this.cartItemRepo = ci;
        this.inventoryRepo = inventoryRepo;
        this.customerRepo = customerRepo;
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.paymentRepo = paymentRepo;
        this.paymentProcessor = paymentProcessor;
    }

    // ---------------- ADD PRODUCT (SCAN) ----------------
    public CartResponse scanProduct(String barcode, String email, int quantity) {
        String trimmedBarcode = barcode != null ? barcode.trim() : "";

        Product product = productRepo.findByBarcode(trimmedBarcode)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product not found for barcode: " + trimmedBarcode));

        Inventory inventory = inventoryRepo.findByProduct_Id(product.getId())
                .orElseThrow(() -> new OutOfStockException(
                        "Inventory not found for: " + product.getName()));

        // Lock the existing customer row before looking for an ACTIVE cart.
        // This serializes first scans even when no cart row exists to lock yet.
        Customer customer = customerRepo.findByEmailForUpdate(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Customer not found for email: " + email));
        Cart cart = cartRepo.findByCustomerIdAndStatusForUpdate(
                        customer.getCustomerId(), CartStatus.ACTIVE)
                .orElse(null);

        // Add or increment existing item
        CartItem item = cart == null ? null : cart.getItems().stream()
                .filter(ci -> ci.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        int requestedCartQuantity = quantity + (item == null ? 0 : item.getQuantity());
        if (requestedCartQuantity > inventory.getQuantity()) {
            throw new OutOfStockException("Not enough stock for: " + product.getName());
        }

        // ✅ Auto-create cart only after the stock check passes.
        if (cart == null) {
            cart = new Cart();
            cart.setCustomer(customer);
            cart.setStatus(CartStatus.ACTIVE);
        }

        if (item == null) {
            item = new CartItem(null, cart, product, quantity);
            cart.getItems().add(item);
        } else {
            item.setQuantity(item.getQuantity() + quantity);
        }

        Cart saved = cartRepo.save(cart);
        return toCartResponse(saved);
    }

    // ---------------- GET ACTIVE CART ----------------
    // ✅ FIX: Return empty cart instead of throwing 500 when user has no cart yet.
    // This happens on first login — the user hasn't scanned anything so no cart
    // row exists. The old code threw RuntimeException("Active cart not found")
    // which crashed the Flutter cart screen on first open.
    public CartResponse getActiveCart(String email) {
        Customer customer = requireCustomer(email);
        return cartRepo.findByCustomer_CustomerIdAndStatus(
                        customer.getCustomerId(), CartStatus.ACTIVE)
                .map(this::toCartResponse)
                .orElse(emptyCartResponse());
    }

    // ---------------- CHECKOUT ----------------
    public CheckoutResponse checkout(String email) {
        return checkout(email, "UPI");
    }

    public CheckoutResponse checkout(String email, String paymentMethod) {
        Customer customer = requireCustomer(email);
        String normalizedPaymentMethod = normalizePaymentMethod(paymentMethod);
        Cart cart = cartRepo.findByCustomerIdAndStatusForUpdate(
                        customer.getCustomerId(), CartStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("No active cart found. Scan some items first."));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot checkout an empty cart.");
        }

        // Lock inventory rows in product-ID order to serialize competing checkouts.
        List<CartItem> cartItems = new ArrayList<>(cart.getItems());
        cartItems.sort(Comparator.comparing(item -> item.getProduct().getId()));

        List<CheckoutLine> checkoutLines = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO.setScale(2);
        for (CartItem cartItem : cartItems) {
            if (cartItem.getQuantity() <= 0) {
                throw new IllegalArgumentException("Cart item quantity must be greater than zero.");
            }

            Inventory inventory = inventoryRepo.findByProductIdForUpdate(
                            cartItem.getProduct().getId())
                    .orElseThrow(() -> new OutOfStockException(
                            "Inventory not found for: " + cartItem.getProduct().getName()));

            if (inventory.getQuantity() < cartItem.getQuantity()) {
                throw new OutOfStockException(
                        "Not enough stock for: " + cartItem.getProduct().getName());
            }

            BigDecimal unitPrice = resolveUnitPrice(cartItem);
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
            checkoutLines.add(new CheckoutLine(cartItem, inventory, unitPrice, subtotal));
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        Order savedOrder = orderRepo.save(order);

        List<OrderItem> orderItems = checkoutLines.stream()
                .map(line -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(savedOrder);
                    orderItem.setProduct(line.cartItem().getProduct());
                    orderItem.setQuantity(line.cartItem().getQuantity());
                    orderItem.setUnitPrice(line.unitPrice());
                    orderItem.setSubtotal(line.subtotal());
                    return orderItem;
                })
                .toList();
        orderItemRepo.saveAll(orderItems);

        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setAmount(totalAmount);
        payment.setPaymentMethod(normalizedPaymentMethod);
        payment.setStatus("PENDING");
        paymentRepo.save(payment);

        // Local demo simulation only: no payment gateway is called and no real charge occurs.
        boolean paymentSucceeded = paymentProcessor.processPayment(
                normalizedPaymentMethod, totalAmount);
        if (!paymentSucceeded) {
            payment.setStatus("FAILED");
            savedOrder.setStatus("FAILED");
            paymentRepo.save(payment);
            orderRepo.save(savedOrder);
            List<CartItemResponse> failedItems = toCheckoutItems(checkoutLines);
            return new CheckoutResponse(cart.getId(), savedOrder.getOrderId(), failedItems, totalAmount.doubleValue(),
                    "FAILED", "Simulated payment failed; inventory was not changed.");
        }

        payment.setStatus("SUCCESS");
        payment.setPaidAt(LocalDateTime.now());
        // Synthetic local-simulator reference only; this is not a gateway transaction ID.
        payment.setTransactionId("SIM-" + UUID.randomUUID());
        paymentRepo.save(payment);

        savedOrder.setStatus("PAID");
        orderRepo.save(savedOrder);

        LocalDateTime inventoryUpdatedAt = LocalDateTime.now();
        for (CheckoutLine line : checkoutLines) {
            line.inventory().setQuantity(
                    line.inventory().getQuantity() - line.cartItem().getQuantity());
            line.inventory().setUpdatedAt(inventoryUpdatedAt);
        }
        inventoryRepo.saveAll(checkoutLines.stream().map(CheckoutLine::inventory).toList());

        cart.setStatus(CartStatus.CHECKED_OUT);
        cartRepo.save(cart);

        return new CheckoutResponse(
                cart.getId(),
                savedOrder.getOrderId(),
                toCheckoutItems(checkoutLines),
                totalAmount.doubleValue(),
                CartStatus.CHECKED_OUT.name(),
                "Checkout successful; payment was simulated locally (no real charge was processed)."
        );
    }

    // ---------------- REMOVE PRODUCT ----------------
    public CartResponse removeProduct(String barcode, String email, int quantity) {
        Product product = productRepo.findByBarcode(barcode)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + barcode));

        Customer customer = requireCustomer(email);
        // ✅ FIX: Return clear error message instead of generic 500
        Cart cart = cartRepo.findByCustomerIdAndStatusForUpdate(
                        customer.getCustomerId(), CartStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("No active cart found for user."));

        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not in cart: " + product.getName()));

        if (item.getQuantity() < quantity) {
            throw new RuntimeException("Cannot remove " + quantity + " items — only "
                    + item.getQuantity() + " in cart.");
        }

        if (item.getQuantity() == quantity) {
            cart.getItems().remove(item);
            cartItemRepo.delete(item);
        } else {
            item.setQuantity(item.getQuantity() - quantity);
        }

        Cart saved = cartRepo.save(cart);
        return toCartResponse(saved);
    }

    // ---------------- HELPERS ----------------

    private Customer requireCustomer(String email) {
        return customerRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Customer not found for email: " + email));
    }

    private String normalizePaymentMethod(String paymentMethod) {
        String normalized = paymentMethod == null
                ? ""
                : paymentMethod.trim().toUpperCase(Locale.ROOT);
        if (!List.of("UPI", "CARD", "CASH").contains(normalized)) {
            throw new IllegalArgumentException("Payment method must be UPI, CARD, or CASH.");
        }
        return normalized;
    }

    private BigDecimal resolveUnitPrice(CartItem cartItem) {
        BigDecimal unitPrice = cartItem.getUnitPrice();
        if (unitPrice == null) {
            unitPrice = cartItem.getProduct().getMrp();
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalStateException(
                    "A valid unit price is missing for: " + cartItem.getProduct().getName());
        }
        return unitPrice.setScale(2);
    }

    private List<CartItemResponse> toCheckoutItems(List<CheckoutLine> checkoutLines) {
        return checkoutLines.stream()
                .map(line -> new CartItemResponse(
                        line.cartItem().getProduct().getBarcode(),
                        nullToEmpty(line.cartItem().getProduct().getName()),
                        line.unitPrice().doubleValue(),
                        line.cartItem().getQuantity()))
                .toList();
    }

    private record CheckoutLine(
            CartItem cartItem,
            Inventory inventory,
            BigDecimal unitPrice,
            BigDecimal subtotal) {
    }

    private CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> new CartItemResponse(
                        item.getProduct().getBarcode(),
                        nullToEmpty(item.getProduct().getName()),
                        item.getProduct().getPrice(),
                        item.getQuantity()
                ))
                .toList();

        double totalAmount = cart.getItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        return new CartResponse(itemResponses, totalAmount);
    }

    // ✅ Returns a valid empty CartResponse — used when user has no cart yet
    private CartResponse emptyCartResponse() {
        return new CartResponse(Collections.emptyList(), 0.0);
    }

    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }
}
