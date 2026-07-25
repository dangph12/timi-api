package com.example.timi_api.application.service;

import com.example.timi_api.application.dto.response.SkuQuantityLogResponse;
import com.example.timi_api.domain.entity.SkuQuantityLog;
import com.example.timi_api.infrastructure.repository.SkuQuantityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SkuQuantityLogService {

    private final SkuQuantityLogRepository skuQuantityLogRepository;

    public Page<SkuQuantityLogResponse> getAll(Pageable pageable) {
        return skuQuantityLogRepository.findAll(pageable)
                .map(this::toResponse);
    }

    public Page<SkuQuantityLogResponse> getBySkuId(Long skuId, Pageable pageable) {
        return skuQuantityLogRepository.findBySku_Id(skuId, pageable)
                .map(this::toResponse);
    }

    private SkuQuantityLogResponse toResponse(SkuQuantityLog log) {
        return new SkuQuantityLogResponse(
                log.getId(),
                log.getSku().getId(),
                log.getSku().getSkuCode(),
                log.getOrder() != null ? log.getOrder().getId() : null,
                log.getOldQuantity(),
                log.getNewQuantity(),
                log.getChangeAmount(),
                log.getType().name(),
                log.getCreatedAt()
        );
    }
}