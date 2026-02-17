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

    // ---------------- ADD PRODUCT WITH QUANTITY ----------------
    public CartResponse scanProduct(String barcode, String userId, int quantity) {
        String trimmedBarcode = barcode != null ? barcode.trim() : "";
        Product product = productRepo.findByBarcode(trimmedBarcode)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        if (product.getStockQuantity() < quantity) {
            throw new OutOfStockException("Not enough stock");
        }

        // Get active cart or create new
        Cart cart = cartRepo.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    newCart.setStatus(CartStatus.ACTIVE);
                    return cartRepo.save(newCart);
                });

        // Check if product already exists in cart
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

        // Reduce stock
        product.setStockQuantity(product.getStockQuantity() - quantity);

        Cart saved = cartRepo.save(cart);
        return toCartResponse(saved);
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
                .mapToDouble(item ->
                        item.getProduct().getPrice() * item.getQuantity())
                .sum();

        return new CartResponse(itemResponses, totalAmount);
    }


    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    // ---------------- GET ACTIVE CART ----------------
    public CartResponse getActiveCart(String userId) {
        Cart cart = cartRepo.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Active cart not found"));
        return toCartResponse(cart);
    }

    // ---------------- CHECKOUT CART ----------------
    public CheckoutResponse checkout(String userId) {
        Cart cart = cartRepo.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Active cart not found"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot checkout empty cart");
        }

        // Calculate total amount
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> new CartItemResponse(
                        item.getProduct().getBarcode(),
                        nullToEmpty(item.getProduct().getName()),
                        item.getProduct().getPrice(),
                        item.getQuantity()
                ))

                .toList();

        double totalAmount = cart.getItems().stream()
                .mapToDouble(item ->
                        item.getProduct().getPrice() * item.getQuantity())
                .sum();


        // Update cart status to CHECKED_OUT
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

    // ---------------- REMOVE PRODUCT FROM CART ----------------
    public Cart removeProduct(String barcode, String userId, int quantity) {
        Product product = productRepo.findByBarcode(barcode)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        Cart cart = cartRepo.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Active cart not found"));

        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found in cart"));

        if (item.getQuantity() < quantity) {
            throw new RuntimeException("Cannot remove more items than in cart");
        }

        // Restore stock
        product.setStockQuantity(product.getStockQuantity() + quantity);

        // Update or remove cart item
        if (item.getQuantity() == quantity) {
            // Remove item entirely
            cart.getItems().remove(item);
            cartItemRepo.delete(item);
        } else {
            // Reduce quantity
            item.setQuantity(item.getQuantity() - quantity);
        }

        return cartRepo.save(cart);
    }
}
