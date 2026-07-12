package com.example.timi_api.infrastructure.event;

import com.example.timi_api.domain.constant.PaymentStatus;
import com.example.timi_api.domain.entity.Order;
import com.example.timi_api.domain.event.PaymentCompletedEvent;
import com.example.timi_api.infrastructure.web.v1.SseController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEventListenerTest {

    @Mock
    private SseController sseController;

    private PaymentEventListener paymentEventListener;

    @BeforeEach
    void setUp() {
        paymentEventListener = new PaymentEventListener(sseController);
    }

    @Test
    void handlePaymentCompleted_callsSseController() {
        Order order = new Order();
        order.setPublicId("ORDER-001");
        order.setPaymentStatus(PaymentStatus.PAID);
        PaymentCompletedEvent event = new PaymentCompletedEvent(this, order);

        paymentEventListener.handlePaymentCompleted(event);

        verify(sseController).sendPaymentStatus("ORDER-001", "PAID");
    }
}
