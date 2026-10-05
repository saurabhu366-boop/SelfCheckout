package com.sourabh.selfcheckout.Controller;

import com.sourabh.selfcheckout.Dto.CartResponse;
import com.sourabh.selfcheckout.Dto.CheckoutResponse;
import com.sourabh.selfcheckout.Dto.RemoveProductRequest;
import com.sourabh.selfcheckout.Dto.ScanRequest;
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

    @GetMapping({"", "/{userId}"})
    public CartResponse getActiveCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        return cartService.getActiveCart(userDetails.getUsername());
    }

    @PostMapping({"/checkout", "/{userId}/checkout"})
    public CheckoutResponse checkout(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "UPI") String paymentMethod) {
        return cartService.checkout(userDetails.getUsername(), paymentMethod);
    }

    @PostMapping("/remove")
    public CartResponse removeProduct(
            @Valid @RequestBody RemoveProductRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return cartService.removeProduct(
                request.getBarcode(),
                userDetails.getUsername(), // ✅ from JWT, not request body
                request.getQuantity()
        );
    }
}
