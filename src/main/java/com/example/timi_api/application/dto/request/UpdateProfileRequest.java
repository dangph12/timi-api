package com.example.timi_api.application.dto.request;

import com.example.timi_api.infrastructure.message.Message;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank(message = Message.NAME_NOT_BLANK)
    private String fullName;

    @NotBlank(message = Message.PHONE_NOT_BLANK)
    private String phone;

    @NotBlank(message = Message.ADDRESS_NOT_BLANK)
    private String address;
}
