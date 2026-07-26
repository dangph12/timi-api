package com.example.timi_api.application.dto.request;

import com.example.timi_api.infrastructure.message.Message;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = Message.EMAIL_NOT_BLANK)
    @Email(message = Message.EMAIL_INVALID)
    private String email;

    @NotBlank(message = Message.PASSWORD_NOT_BLANK)
    private String password;
}
