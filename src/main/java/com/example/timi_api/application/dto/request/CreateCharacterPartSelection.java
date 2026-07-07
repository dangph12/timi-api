package com.example.timi_api.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCharacterPartSelection {

    @NotNull
    private Long partOptionId;
}
