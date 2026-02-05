package com.sourabh.selfcheckout.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScanRequest {
    @NotBlank
    private String barcode;

    @NotBlank
    private String userId;
}

