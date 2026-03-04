package com.sourabh.selfcheckout.Dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class ScanRequest {

    @NotBlank
    private String barcode;

    // ✅ REMOVED: userId — now comes from JWT via @AuthenticationPrincipal

    @Min(1)
    private Integer quantity = 1;

    public String getBarcode() { return barcode; }

    public int getQuantity() { return quantity == null ? 1 : quantity; }

    public void setBarcode(String barcode) { this.barcode = barcode; }

    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}