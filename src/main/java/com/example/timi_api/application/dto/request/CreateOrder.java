package com.example.timi_api.application.dto.request;

import com.example.timi_api.domain.constant.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrder {

    @NotBlank
    private String email;

    @NotBlank
    private String name;

    @NotBlank
    private String phone;

    @NotBlank
    private String address;

    private Long accountId;

    private String note;

    private PaymentMethod paymentMethod;

    @NotEmpty
    private List<@Valid CreateOrderItem> items;
}
