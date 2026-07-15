package com.example.timi_api.application.dto.request;

import com.example.timi_api.infrastructure.message.Message;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateCharacterDesign {
    @NotBlank(message = Message.DESIGN_NAME_NOT_BLANK)
    private String name;

    @NotBlank(message = Message.IMAGE_URL_NOT_BLANK)
    private String imageUrl;

    @NotEmpty(message = Message.PART_SELECTIONS_NOT_EMPTY)
    private List<@Valid CreateCharacterPartSelection> partSelections;
}
