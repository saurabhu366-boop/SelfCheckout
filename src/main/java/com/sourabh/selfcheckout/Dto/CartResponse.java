package com.sourabh.selfcheckout.Dto;

import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
public class CartResponse {
    private List<CartItemResponse> items;
    private double totalAmount;

}

