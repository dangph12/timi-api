package com.example.timi_api.infrastructure.web.v1;

import com.example.timi_api.application.dto.request.AdjustSkuQuantityRequest;
import com.example.timi_api.application.dto.request.CreateSkuRequest;
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

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SkuResponse>> getSkuById(@PathVariable Long id) {
        SkuResponse sku = skuService.getSkuById(id);
        return ResponseEntity.ok(ApiResponse.success(Message.GET_SKU_SUCCESS, sku));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SkuResponse>> createSku(@RequestBody @Valid CreateSkuRequest request) {
        SkuResponse sku = skuService.createSku(request.getSkuCode(), request.getCategoryId(), request.getSizeId(), request.getPrice(), request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success(Message.SKU_CREATED, sku));
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
        skuService.adjustQuantity(id, request.getQuantity(), request.getLogType(), null);
        return ResponseEntity.ok(ApiResponse.success(Message.SKU_QUANTITY_ADJUSTED));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSku(@PathVariable Long id) {
        skuService.deleteSku(id);
        return ResponseEntity.ok(ApiResponse.success(Message.SKU_DELETED));
    }
}
