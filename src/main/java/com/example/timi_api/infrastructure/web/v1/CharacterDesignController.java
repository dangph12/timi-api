package com.example.timi_api.infrastructure.web.v1;

import com.example.timi_api.application.dto.request.CreateCharacterDesign;
import com.example.timi_api.application.service.CharacterDesignService;
import com.example.timi_api.domain.entity.CharacterDesign;
import com.example.timi_api.infrastructure.common.ApiResponse;
import com.example.timi_api.infrastructure.message.Message;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/designs")
@RequiredArgsConstructor
public class CharacterDesignController {

    private final CharacterDesignService characterDesignService;

    @PostMapping
    public ResponseEntity<ApiResponse<CharacterDesign>> create(@Valid @RequestBody CreateCharacterDesign request) {
        CharacterDesign design = characterDesignService.createCharacterDesign(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Message.CHARACTER_DESIGN_CREATED, design));
    }
}
