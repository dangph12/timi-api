package com.example.timi_api.infrastructure.web.v1;

import com.example.timi_api.application.dto.request.CreateOrder;
import com.example.timi_api.application.dto.response.OrderListItemResponse;
import com.example.timi_api.application.dto.response.OrderResponse;
import com.example.timi_api.application.service.OrderService;
import com.example.timi_api.domain.constant.OrderStatus;
import com.example.timi_api.infrastructure.common.ApiResponse;
import com.example.timi_api.infrastructure.message.Message;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<OrderListItemResponse>>> getMyOrders(
            @AuthenticationPrincipal Long accountId,
            @RequestParam(required = false) OrderStatus status,
            Pageable pageable) {
        Page<OrderListItemResponse> orders = orderService.getMyOrders(accountId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(Message.LIST_ORDERS_SUCCESS, orders));
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable String publicId) {
        OrderResponse order = orderService.getOrderByPublicId(publicId);
        return ResponseEntity.ok(ApiResponse.success(Message.GET_ORDER_SUCCESS, order));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal Long accountId,
            @Valid @RequestBody CreateOrder request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        OrderResponse order;
        try {
            order = orderService.createOrder(request, idempotencyKey, accountId);
        } catch (DataIntegrityViolationException e) {
            if (idempotencyKey == null) {
                throw e;
            }
            order = orderService.getOrderByIdempotencyKey(idempotencyKey);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Message.ORDER_CREATED, order));
    }

    @PostMapping("/{publicId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable String publicId) {
        OrderResponse order = orderService.cancelOrder(publicId);
        return ResponseEntity.ok(ApiResponse.success(Message.ORDER_CANCELLED, order));
    }

    @PostMapping("/{publicId}/confirm-payment")
    public ResponseEntity<ApiResponse<OrderResponse>> confirmCodPayment(
            @PathVariable String publicId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        OrderResponse order;
        try {
            order = orderService.selectCodPayment(publicId, idempotencyKey);
        } catch (DataIntegrityViolationException e) {
            order = orderService.getOrderByPublicId(publicId);
        }
        return ResponseEntity.ok(ApiResponse.success(Message.PAYMENT_SUCCESS, order));
    }
}
