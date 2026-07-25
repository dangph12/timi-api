package com.example.timi_api.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateSkuRequest {

    @NotBlank
    private String skuCode;

    @NotNull
    private Long categoryId;

    @NotNull
    private Long sizeId;

    @NotNull
    private BigDecimal price;

    @Min(0)
    @NotNull
    private Integer quantity;
}