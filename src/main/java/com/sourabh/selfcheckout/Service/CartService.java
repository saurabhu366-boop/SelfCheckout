package com.sourabh.selfcheckout.Service;

import com.sourabh.selfcheckout.Dto.CartItemResponse;
import com.sourabh.selfcheckout.Dto.CartResponse;
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
    public Cart scanProduct(String barcode, String userId, int quantity) {

        Product product = productRepo.findByBarcode(barcode)
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

        return cartRepo.save(cart);
    }

    // ---------------- GET ACTIVE CART ----------------
    public CartResponse getActiveCart(String userId) {

        Cart cart = cartRepo.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Active cart not found"));

        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> new CartItemResponse(
                        item.getProduct().getName(),
                        item.getProduct().getPrice() * item.getQuantity(),
                        item.getQuantity()
                ))
                .toList();

        double totalAmount = itemResponses.stream()
                .mapToDouble(CartItemResponse::getPrice)
                .sum();

        return new CartResponse(itemResponses, totalAmount);
    }
}
