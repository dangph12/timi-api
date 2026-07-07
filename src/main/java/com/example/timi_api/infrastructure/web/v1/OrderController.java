package com.example.timi_api.infrastructure.web.v1;

import com.example.timi_api.application.dto.request.CreateOrder;
import com.example.timi_api.application.service.OrderService;
import com.example.timi_api.domain.entity.Order;
import com.example.timi_api.infrastructure.common.ApiResponse;
import com.example.timi_api.infrastructure.message.Message;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<Order>> createOrder(@Valid @RequestBody CreateOrder request) {
        Order order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Message.ORDER_CREATED, order));
    }
}
