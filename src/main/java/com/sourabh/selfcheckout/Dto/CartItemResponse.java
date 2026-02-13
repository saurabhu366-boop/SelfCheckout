package com.sourabh.selfcheckout.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartItemResponse {

    private String productName;
    private double price;
    private int quantity;
}
