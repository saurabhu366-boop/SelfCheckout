package com.sourabh.selfcheckout.Dto;

// ✅ FIX Bug 7: Removed stray `import jakarta.persistence.Entity` — this is a DTO,
//    not a JPA entity. The import was unused and misleading.

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddToCartRequest {

    private Long cartId;
    private Long productId;
    private int quantity;

    public Long getCartId() { return cartId; }
    public void setCartId(Long cartId) { this.cartId = cartId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}