package com.example.timi_api.application.service;

import com.example.timi_api.application.dto.response.SkuTransactionResponse;
import com.example.timi_api.infrastructure.repository.SkuTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SkuTransactionService {

    private final SkuTransactionRepository skuTransactionRepository;

    public Page<SkuTransactionResponse> getAll(Pageable pageable) {
        return skuTransactionRepository.findAll(pageable)
                .map(this::toResponse);
    }

    public Page<SkuTransactionResponse> getBySkuId(Long skuId, Pageable pageable) {
        return skuTransactionRepository.findBySku_Id(skuId, pageable)
                .map(this::toResponse);
    }

    private SkuTransactionResponse toResponse(com.example.timi_api.domain.entity.SkuTransaction t) {
        return new SkuTransactionResponse(
                t.getId(),
                t.getSku().getId(),
                t.getSku().getSkuCode(),
                t.getOrder() != null ? t.getOrder().getId() : null,
                t.getOldQuantity(),
                t.getNewQuantity(),
                t.getChangeAmount(),
                t.getTransactionType().name(),
                t.getCreatedAt()
        );
    }
}