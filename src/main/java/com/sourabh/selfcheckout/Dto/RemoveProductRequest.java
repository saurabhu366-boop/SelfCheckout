package com.sourabh.selfcheckout.Dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class RemoveProductRequest {

    @NotBlank
    private String barcode;

    // ✅ REMOVED: userId — now comes from JWT via @AuthenticationPrincipal

    @Min(1)
    private int quantity;

    public String getBarcode() { return barcode; }

    public int getQuantity() { return quantity; }

    public void setBarcode(String barcode) { this.barcode = barcode; }

    public void setQuantity(int quantity) { this.quantity = quantity; }
}