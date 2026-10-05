package com.sourabh.selfcheckout.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class OrderHistoryResponse {
    private Long orderId;
    private LocalDateTime createdAt;
    private List<OrderHistoryItemResponse> items;
    private double totalAmount;
    private String status;
}
