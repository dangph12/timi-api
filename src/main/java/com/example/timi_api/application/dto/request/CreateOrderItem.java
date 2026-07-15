package com.example.timi_api.application.dto.request;

import com.example.timi_api.infrastructure.message.Message;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderItem {

    @NotNull(message = Message.SKU_ID_NOT_NULL)
    private Long skuId;

    @NotNull(message = Message.DESIGN_ID_NOT_NULL)
    private Long characterDesignId;

    @Min(value = 1, message = Message.QUANTITY_MIN_ONE)
    private Integer quantity;
}
