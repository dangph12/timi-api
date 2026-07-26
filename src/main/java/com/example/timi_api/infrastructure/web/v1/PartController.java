package com.example.timi_api.infrastructure.web.v1;

import com.example.timi_api.application.dto.response.PartOptionResponse;
import com.example.timi_api.application.dto.response.PartResponse;
import com.example.timi_api.application.service.PartService;
import com.example.timi_api.infrastructure.common.ApiResponse;
import com.example.timi_api.infrastructure.message.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/parts")
@RequiredArgsConstructor
public class PartController {

    private final PartService partService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PartResponse>>> getAllParts() {
        List<PartResponse> parts = partService.getAllParts();
        return ResponseEntity.ok(ApiResponse.success(Message.LIST_PARTS_SUCCESS, parts));
    }

    @GetMapping("/{partId}/options")
    public ResponseEntity<ApiResponse<List<PartOptionResponse>>> getPartOptions(
            @PathVariable Long partId,
            @RequestParam(required = false) Long styleId) {
        List<PartOptionResponse> options = partService.getPartOptions(partId, styleId);
        return ResponseEntity.ok(ApiResponse.success(Message.LIST_PART_OPTIONS_SUCCESS, options));
    }
}
