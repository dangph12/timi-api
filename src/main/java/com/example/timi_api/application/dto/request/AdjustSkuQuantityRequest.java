package com.example.timi_api.application.dto.request;

import com.example.timi_api.domain.constant.SkuTransactionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdjustSkuQuantityRequest {

    @Min(1)
    @NotNull
    private Integer quantity;

    @NotNull
    private SkuTransactionType transactionType;
}
