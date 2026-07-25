package com.example.timi_api.infrastructure.web.v1;

import com.example.timi_api.application.dto.response.SizeResponse;
import com.example.timi_api.application.service.SizeService;
import com.example.timi_api.infrastructure.common.ApiResponse;
import com.example.timi_api.infrastructure.message.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/sizes")
@RequiredArgsConstructor
public class SizeController {

    private final SizeService sizeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SizeResponse>>> getAllSizes() {
        List<SizeResponse> sizes = sizeService.getAllSizes();
        return ResponseEntity.ok(ApiResponse.success(Message.LIST_SIZES_SUCCESS, sizes));
    }
}