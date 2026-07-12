package com.example.timi_api.infrastructure.event;

import com.example.timi_api.domain.event.PaymentCompletedEvent;
import com.example.timi_api.infrastructure.web.v1.SseController;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final SseController sseController;

    @EventListener
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        sseController.sendPaymentStatus(
                event.getOrder().getPublicId(),
                event.getOrder().getPaymentStatus().name()
        );
    }
}
