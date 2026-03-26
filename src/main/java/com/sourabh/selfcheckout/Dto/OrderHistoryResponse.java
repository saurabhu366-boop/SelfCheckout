package com.sourabh.selfcheckout.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class OrderHistoryResponse {
    private Long cartId;
    private LocalDateTime createdAt;
    private List<CartItemResponse> items;
    private double totalAmount;
    private String status;
}