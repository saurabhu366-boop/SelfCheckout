package com.sourabh.selfcheckout.Dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class RemoveProductRequest {

    @NotBlank
    private String barcode;

    @NotBlank
    private String userId;

    @Min(1)
    private int quantity;

    public String getBarcode() {
        return barcode;
    }

    public String getUserId() {
        return userId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
