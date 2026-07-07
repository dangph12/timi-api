package com.example.timi_api.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateCharacterDesign {
    @NotBlank
    private String name;

    @NotBlank
    private String imageUrl;

    @NotEmpty
    private List<@Valid CreateCharacterPartSelection> partSelections;
}
