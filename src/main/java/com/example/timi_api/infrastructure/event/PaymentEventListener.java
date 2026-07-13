package com.example.timi_api.infrastructure.event;

import com.example.timi_api.domain.event.PaymentCompletedEvent;
import com.example.timi_api.infrastructure.email.EmailService;
import com.example.timi_api.infrastructure.web.v1.SseController;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final SseController sseController;
    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        sseController.sendPaymentStatus(
                event.getOrder().getPublicId(),
                event.getOrder().getPaymentStatus().name()
        );
        emailService.sendOrderConfirmation(event.getOrder());
    }
}
