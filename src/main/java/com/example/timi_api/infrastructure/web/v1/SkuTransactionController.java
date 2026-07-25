package com.example.timi_api.infrastructure.web.v1;

import com.example.timi_api.application.dto.response.SkuTransactionResponse;
import com.example.timi_api.application.service.SkuTransactionService;
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
@RequestMapping("/v1/sku-transactions")
@RequiredArgsConstructor
public class SkuTransactionController {

    private final SkuTransactionService skuTransactionService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<SkuTransactionResponse>>> getSkuTransactions(
            @RequestParam(required = false) Long skuId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<SkuTransactionResponse> transactions;
        if (skuId != null) {
            transactions = skuTransactionService.getBySkuId(skuId, pageable);
        } else {
            transactions = skuTransactionService.getAll(pageable);
        }
        return ResponseEntity.ok(ApiResponse.success(Message.LIST_SKU_TRANSACTIONS_SUCCESS, transactions));
    }
}