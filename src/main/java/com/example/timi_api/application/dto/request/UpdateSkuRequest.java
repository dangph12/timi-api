package com.example.timi_api.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateSkuRequest {

    @NotBlank
    private String skuCode;

    @NotNull
    private Long categoryId;

    @NotNull
    private Long sizeId;

    @NotNull
    private BigDecimal price;
}
