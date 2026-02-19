package com.sourabh.selfcheckout.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class CheckoutResponse {
    private Long cartId;
    private String userId;
    private List<CartItemResponse> items;
    private double totalAmount;
    private String status;
    private String message;
}
