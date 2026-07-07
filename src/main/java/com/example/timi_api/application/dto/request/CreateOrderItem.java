package com.example.timi_api.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderItem {

    @NotNull
    private Long skuId;

    @NotNull
    private Long characterDesignId;

    @Min(1)
    private Integer quantity;
}
