package com.example.timi_api.domain.event;

import com.example.timi_api.domain.entity.Order;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaymentCompletedEventTest {
    @Test
    void testCreation() {
        Order order = new Order();
        order.setId(1L);
        PaymentCompletedEvent event = new PaymentCompletedEvent(this, order);
        assertEquals(order, event.getOrder());
        assertNotNull(event.getSource());
    }
}
