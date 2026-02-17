package com.sourabh.selfcheckout.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class CartItemResponse {

    private String barcode;
    private String productName;
    private double price;
    private int quantity;

    public CartItemResponse(String barcode,
                            String productName,
                            double price,
                            int quantity) {
        this.barcode = barcode;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
    }
}

