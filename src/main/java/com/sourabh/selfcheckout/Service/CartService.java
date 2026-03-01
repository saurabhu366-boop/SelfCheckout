package com.sourabh.selfcheckout.Service;

import com.sourabh.selfcheckout.Dto.CartItemResponse;
import com.sourabh.selfcheckout.Dto.CartResponse;
import com.sourabh.selfcheckout.Dto.CheckoutResponse;
import com.sourabh.selfcheckout.Entity.Cart;
import com.sourabh.selfcheckout.Entity.CartItem;
import com.sourabh.selfcheckout.Entity.Product;
import com.sourabh.selfcheckout.Enum.CartStatus;
import com.sourabh.selfcheckout.Exception.OutOfStockException;
import com.sourabh.selfcheckout.Exception.ProductNotFoundException;
import com.sourabh.selfcheckout.Repo.CartItemRepository;
import com.sourabh.selfcheckout.Repo.CartRepository;
import com.sourabh.selfcheckout.Repo.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class CartService {

    private final ProductRepository productRepo;
    private final CartRepository cartRepo;
    private final CartItemRepository cartItemRepo;

    public CartService(ProductRepository p, CartRepository c, CartItemRepository ci) {
        this.productRepo = p;
        this.cartRepo = c;
        this.cartItemRepo = ci;
    }

    // ---------------- ADD PRODUCT (SCAN) ----------------
    public CartResponse scanProduct(String barcode, String userId, int quantity) {
        String trimmedBarcode = barcode != null ? barcode.trim() : "";

        Product product = productRepo.findByBarcode(trimmedBarcode)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product not found for barcode: " + trimmedBarcode));

        if (product.getStockQuantity() < quantity) {
            throw new OutOfStockException("Not enough stock for: " + product.getName());
        }

        // ✅ Auto-create cart if none exists — no pre-inserted SQL row needed
        Cart cart = cartRepo.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    newCart.setStatus(CartStatus.ACTIVE);
                    return cartRepo.save(newCart);
                });

        // Add or increment existing item
        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (item == null) {
            item = new CartItem(null, cart, product, quantity);
            cart.getItems().add(item);
        } else {
            item.setQuantity(item.getQuantity() + quantity);
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);

        Cart saved = cartRepo.save(cart);
        return toCartResponse(saved);
    }

    // ---------------- GET ACTIVE CART ----------------
    // ✅ FIX: Return empty cart instead of throwing 500 when user has no cart yet.
    // This happens on first login — the user hasn't scanned anything so no cart
    // row exists. The old code threw RuntimeException("Active cart not found")
    // which crashed the Flutter cart screen on first open.
    public CartResponse getActiveCart(String userId) {
        return cartRepo.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .map(this::toCartResponse)
                .orElse(emptyCartResponse());
    }

    // ---------------- CHECKOUT ----------------
    public CheckoutResponse checkout(String userId) {
        // ✅ FIX: Return clear error message instead of generic 500
        Cart cart = cartRepo.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("No active cart found. Scan some items first."));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot checkout an empty cart.");
        }

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

        cart.setStatus(CartStatus.CHECKED_OUT);
        cartRepo.save(cart);

        return new CheckoutResponse(
                cart.getId(),
                nullToEmpty(cart.getUserId()),
                itemResponses,
                totalAmount,
                CartStatus.CHECKED_OUT.name(),
                "Checkout successful"
        );
    }

    // ---------------- REMOVE PRODUCT ----------------
    public Cart removeProduct(String barcode, String userId, int quantity) {
        Product product = productRepo.findByBarcode(barcode)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + barcode));

        // ✅ FIX: Return clear error message instead of generic 500
        Cart cart = cartRepo.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("No active cart found for user."));

        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not in cart: " + product.getName()));

        if (item.getQuantity() < quantity) {
            throw new RuntimeException("Cannot remove " + quantity + " items — only "
                    + item.getQuantity() + " in cart.");
        }

        product.setStockQuantity(product.getStockQuantity() + quantity);

        if (item.getQuantity() == quantity) {
            cart.getItems().remove(item);
            cartItemRepo.delete(item);
        } else {
            item.setQuantity(item.getQuantity() - quantity);
        }

        return cartRepo.save(cart);
    }

    // ---------------- HELPERS ----------------

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