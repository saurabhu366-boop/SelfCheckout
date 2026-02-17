package com.sourabh.selfcheckout.Dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class ScanRequest {

    @NotBlank
    private String barcode;

    @NotBlank
    private String userId;

    @Min(1)
    private Integer quantity = 1;

    public String getBarcode() {
        return barcode;
    }

    public String getUserId() {
        return userId;
    }

    /** Quantity to add; defaults to 1 if not sent (e.g. single scan). */
    public int getQuantity() {
        return quantity == null ? 1 : quantity;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
