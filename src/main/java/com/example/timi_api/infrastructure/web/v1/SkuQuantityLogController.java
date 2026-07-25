package com.example.timi_api.infrastructure.web.v1;

import com.example.timi_api.application.dto.response.SkuQuantityLogResponse;
import com.example.timi_api.application.service.SkuQuantityLogService;
import com.example.timi_api.infrastructure.common.ApiResponse;
import com.example.timi_api.infrastructure.message.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/sku-quantity-logs")
@RequiredArgsConstructor
public class SkuQuantityLogController {

    private final SkuQuantityLogService skuQuantityLogService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<SkuQuantityLogResponse>>> getSkuQuantityLogs(
            @RequestParam(required = false) Long skuId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<SkuQuantityLogResponse> logs;
        if (skuId != null) {
            logs = skuQuantityLogService.getBySkuId(skuId, pageable);
        } else {
            logs = skuQuantityLogService.getAll(pageable);
        }
        return ResponseEntity.ok(ApiResponse.success(Message.LIST_SKU_QUANTITY_LOGS_SUCCESS, logs));
    }
}