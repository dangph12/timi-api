package com.example.timi_api.application.dto.response;

import com.example.timi_api.domain.constant.OrderStatus;
import com.example.timi_api.domain.constant.PaymentMethod;
import com.example.timi_api.domain.constant.PaymentStatus;
import com.example.timi_api.domain.entity.Account;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class OrderResponse {
    String publicId;
    Account account;
    String email;
    String name;
    String phone;
    String address;
    String note;
    BigDecimal totalAmount;
    LocalDateTime createdAt;
    LocalDateTime expiresAt;
    OrderStatus currentStatus;
    PaymentStatus currentPaymentStatus;
    PaymentMethod paymentMethod;
    List<OrderItemResponse> items;
    List<OrderStatusHistoryResponse> statusHistory;
}
