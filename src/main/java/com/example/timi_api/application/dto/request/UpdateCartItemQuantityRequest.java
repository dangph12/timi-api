package com.example.timi_api.application.dto.request;

import com.example.timi_api.infrastructure.message.Message;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateCartItemQuantityRequest {

    @Min(value = 1, message = Message.QUANTITY_MIN_ONE)
    private Integer quantity;
}
