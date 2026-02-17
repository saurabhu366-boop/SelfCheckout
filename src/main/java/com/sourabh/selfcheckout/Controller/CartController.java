package com.sourabh.selfcheckout.Controller;



import com.sourabh.selfcheckout.Dto.CartResponse;
import com.sourabh.selfcheckout.Dto.CheckoutResponse;
import com.sourabh.selfcheckout.Dto.RemoveProductRequest;
import com.sourabh.selfcheckout.Dto.ScanRequest;
import com.sourabh.selfcheckout.Entity.Cart;
import com.sourabh.selfcheckout.Service.CartService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;



@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /** Call this when user scans a product (e.g. with camera). Returns updated cart (items + total) so the frontend can update the cart UI. */
    @PostMapping("/scan")
    public CartResponse scan(@Valid @RequestBody ScanRequest request) {
        String barcode = request.getBarcode() != null ? request.getBarcode().trim() : "";
        String userId = request.getUserId() != null ? request.getUserId().trim() : "";
        if (barcode.isEmpty() || userId.isEmpty()) {
            throw new IllegalArgumentException("barcode and userId are required");
        }
        return cartService.scanProduct(barcode, userId, request.getQuantity());
    }

    @GetMapping("/{userId}")
    public CartResponse getActiveCart(@PathVariable String userId) {
        return cartService.getActiveCart(userId);
    }

    @PostMapping("/{userId}/checkout")
    public CheckoutResponse checkout(@PathVariable String userId) {
        return cartService.checkout(userId);
    }

    @PostMapping("/remove")
    public Cart removeProduct(@Valid @RequestBody RemoveProductRequest request) {
        return cartService.removeProduct(
                request.getBarcode(),
                request.getUserId(),
                request.getQuantity()


        );
    }


}

