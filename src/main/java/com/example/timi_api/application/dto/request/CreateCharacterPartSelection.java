package com.example.timi_api.application.dto.request;

import com.example.timi_api.infrastructure.message.Message;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCharacterPartSelection {

    @NotNull(message = Message.PART_OPTION_ID_NOT_NULL)
    private Long partOptionId;
}
