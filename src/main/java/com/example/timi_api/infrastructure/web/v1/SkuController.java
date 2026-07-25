package com.example.timi_api.infrastructure.web.v1;

import com.example.timi_api.application.dto.request.AdjustSkuQuantityRequest;
import com.example.timi_api.application.dto.request.UpdateSkuRequest;
import com.example.timi_api.application.dto.response.SkuResponse;
import com.example.timi_api.application.service.SkuService;
import com.example.timi_api.infrastructure.common.ApiResponse;
import com.example.timi_api.infrastructure.message.Message;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/skus")
@RequiredArgsConstructor
public class SkuController {
    private final SkuService skuService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SkuResponse>>> getAllSkus(
            @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<SkuResponse> skus = skuService.getAllSkus(pageable);
        return ResponseEntity.ok(ApiResponse.success(Message.LIST_SKUS_SUCCESS, skus));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateSku(@PathVariable Long id, @RequestBody @Valid UpdateSkuRequest request) {
        skuService.updateSku(id, request.getSkuCode(), request.getCategoryId(), request.getSizeId(), request.getPrice());
        return ResponseEntity.ok(ApiResponse.success(Message.SKU_UPDATED));
    }

    @PostMapping("/{id}/adjust")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> adjustQuantity(@PathVariable Long id, @RequestBody @Valid AdjustSkuQuantityRequest request) {
        skuService.adjustQuantity(id, request.getQuantity(), request.getTransactionType());
        return ResponseEntity.ok(ApiResponse.success(Message.SKU_QUANTITY_ADJUSTED));
    }
}
