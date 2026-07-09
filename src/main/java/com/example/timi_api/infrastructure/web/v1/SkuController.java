package com.example.timi_api.infrastructure.web.v1;

import com.example.timi_api.application.dto.response.SkuResponse;
import com.example.timi_api.application.service.SkuService;
import com.example.timi_api.infrastructure.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/skus")
@RequiredArgsConstructor
public class SkuController {
    private final SkuService skuService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SkuResponse>>> getAllSkus() {
        List<SkuResponse> skus = skuService.getAllSkus();
        return ResponseEntity.ok(ApiResponse.success("success", skus));
    }
}
