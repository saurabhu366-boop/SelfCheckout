package com.sourabh.selfcheckout.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class OrderHistoryItemResponse {

    private String barcode;
    private String productName;
    private double price;
    private int quantity;
    private BigDecimal subtotal;
}
