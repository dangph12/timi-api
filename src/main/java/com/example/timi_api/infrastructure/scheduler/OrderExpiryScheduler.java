package com.example.timi_api.infrastructure.scheduler;

import com.example.timi_api.domain.constant.OrderStatus;
import com.example.timi_api.domain.constant.PaymentStatus;
import com.example.timi_api.domain.entity.Order;
import com.example.timi_api.domain.entity.OrderStatusHistory;
import com.example.timi_api.infrastructure.message.Message;
import com.example.timi_api.infrastructure.repository.OrderRepository;
import com.example.timi_api.infrastructure.repository.OrderStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExpiryScheduler {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void cancelExpiredQrOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);
        List<Order> expired = orderRepository
                .findByCurrentPaymentStatusAndCreatedAtBefore(PaymentStatus.PENDING, cutoff);

        for (Order order : expired) {
            if (order.getPaymentMethod() != null && order.getPaymentMethod().name().equals("QR")) {
                log.info("Cancelling expired QR order {}", order.getPublicId());
                order.setCurrentStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);

                orderStatusHistoryRepository.save(OrderStatusHistory.builder()
                        .order(order)
                        .status(OrderStatus.CANCELLED)
                        .note(Message.QR_EXPIRED_NOTE)
                        .createdAt(LocalDateTime.now())
                        .build());
            }
        }
    }
}
