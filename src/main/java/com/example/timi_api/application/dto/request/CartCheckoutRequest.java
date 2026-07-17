package com.example.timi_api.application.dto.request;

import com.example.timi_api.infrastructure.message.Message;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CartCheckoutRequest {

    @NotEmpty(message = Message.CART_ITEMS_NOT_EMPTY)
    private List<@NotNull Long> cartItemIds;

    @NotBlank(message = Message.EMAIL_NOT_BLANK)
    private String email;

    @NotBlank(message = Message.NAME_NOT_BLANK)
    private String name;

    @NotBlank(message = Message.PHONE_NOT_BLANK)
    private String phone;

    @NotBlank(message = Message.ADDRESS_NOT_BLANK)
    private String address;

    private String note;
}
