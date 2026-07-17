package com.example.timi_api.application.dto.response;

import com.example.timi_api.domain.constant.OrderStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class OrderListItemResponse {
    String publicId;
    OrderStatus currentStatus;
    BigDecimal totalAmount;
    LocalDateTime createdAt;
    int itemCount;
}
