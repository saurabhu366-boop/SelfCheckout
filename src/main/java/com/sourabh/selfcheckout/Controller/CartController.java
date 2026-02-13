package com.sourabh.selfcheckout.Controller;



import com.sourabh.selfcheckout.Dto.CartResponse;
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

    @PostMapping("/scan")
    public Cart scan(@Valid @RequestBody ScanRequest request) {
        return cartService.scanProduct(
                request.getBarcode(),
                request.getUserId(),
                request.getQuantity()
        );
    }

    @GetMapping("/{userId}")
    public CartResponse getActiveCart(@PathVariable String userId) {
        return cartService.getActiveCart(userId);
    }


}

