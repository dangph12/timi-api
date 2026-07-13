package com.example.timi_api.application.dto.response;

import com.example.timi_api.domain.constant.OrderStatus;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class OrderStatusHistoryResponse {
    Long id;
    OrderStatus status;
    LocalDateTime createdAt;
    String note;
}
