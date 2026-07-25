package com.example.timi_api.application.dto.response;

import java.time.LocalDateTime;

public record SkuQuantityLogResponse(
        Long id,
        Long skuId,
        String skuCode,
        Long orderId,
        Integer oldQuantity,
        Integer newQuantity,
        Integer changeAmount,
        String type,
        LocalDateTime createdAt
) {
}