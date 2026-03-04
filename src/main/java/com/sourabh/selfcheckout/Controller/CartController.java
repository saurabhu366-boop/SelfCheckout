package com.sourabh.selfcheckout.Controller;

import com.sourabh.selfcheckout.Dto.CartResponse;
import com.sourabh.selfcheckout.Dto.CheckoutResponse;
import com.sourabh.selfcheckout.Dto.RemoveProductRequest;
import com.sourabh.selfcheckout.Dto.ScanRequest;
import com.sourabh.selfcheckout.Entity.Cart;
import com.sourabh.selfcheckout.Service.CartService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/scan")
    public CartResponse scan(
            @Valid @RequestBody ScanRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = userDetails.getUsername(); // ✅ from JWT, not request body
        String barcode = request.getBarcode().trim();

        if (barcode.isEmpty()) {
            throw new IllegalArgumentException("barcode is required");
        }
        return cartService.scanProduct(barcode, userId, request.getQuantity());
    }

    @GetMapping                                    // ✅ was /{userId} — caused 404 with emails
    public CartResponse getActiveCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        return cartService.getActiveCart(userDetails.getUsername());
    }

    @PostMapping("/checkout")                      // ✅ was /{userId}/checkout
    public CheckoutResponse checkout(
            @AuthenticationPrincipal UserDetails userDetails) {
        return cartService.checkout(userDetails.getUsername());
    }

    @PostMapping("/remove")
    public Cart removeProduct(
            @Valid @RequestBody RemoveProductRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return cartService.removeProduct(
                request.getBarcode(),
                userDetails.getUsername(), // ✅ from JWT, not request body
                request.getQuantity()
        );
    }
}