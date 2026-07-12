package com.example.timi_api.infrastructure.web.v1;

import com.example.timi_api.domain.entity.Order;
import com.example.timi_api.infrastructure.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SseControllerTest {

    @Mock
    private OrderRepository orderRepository;

    private SseController sseController;

    @BeforeEach
    void setUp() {
        sseController = new SseController(orderRepository);
    }

    @Test
    void subscribe_orderExists_returnsSseEmitter() {
        when(orderRepository.findByPublicId("ORDER-001")).thenReturn(Optional.of(new Order()));

        SseEmitter emitter = sseController.subscribe("ORDER-001");

        assertNotNull(emitter);
    }

    @Test
    void subscribe_orderNotFound_throwsException() {
        when(orderRepository.findByPublicId("NONEXISTENT")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> sseController.subscribe("NONEXISTENT"));
    }

    @Test
    void subscribe_callsRepository() {
        Order order = new Order();
        order.setPublicId("ORDER-001");
        when(orderRepository.findByPublicId("ORDER-001")).thenReturn(Optional.of(order));

        sseController.subscribe("ORDER-001");

        verify(orderRepository).findByPublicId("ORDER-001");
    }

    @Test
    void sendPaymentStatus_emitterExists_sendsEvent() throws Exception {
        Order order = new Order();
        order.setPublicId("ORDER-001");
        when(orderRepository.findByPublicId("ORDER-001")).thenReturn(Optional.of(order));

        sseController.subscribe("ORDER-001");
        sseController.sendPaymentStatus("ORDER-001", "PAID");
    }

    @Test
    void sendPaymentStatus_emitterDoesNotExist_noException() {
        sseController.sendPaymentStatus("NONEXISTENT", "PAID");
    }
}
