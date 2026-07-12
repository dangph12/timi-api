package com.example.timi_api.domain.event;

import com.example.timi_api.domain.entity.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PaymentCompletedEvent extends ApplicationEvent {

    private final Order order;

    public PaymentCompletedEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
}
