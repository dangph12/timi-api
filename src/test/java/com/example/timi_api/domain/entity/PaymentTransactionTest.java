package com.example.timi_api.domain.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.example.timi_api.domain.constant.PaymentMethod;
import com.example.timi_api.domain.constant.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

class PaymentTransactionTest {
    @Test
    void testCreation() {
        Order order = Order.builder().id(1L).build();
        PaymentTransaction transaction = PaymentTransaction.builder()
            .order(order)
            .amount(new BigDecimal("100000"))
            .method(PaymentMethod.QR)
            .status(PaymentStatus.UNPAID)
            .transactionReference("txn_123")
            .createdAt(LocalDateTime.now())
            .build();

        assertNotNull(transaction);
        assertEquals(order, transaction.getOrder());
        assertEquals(PaymentMethod.QR, transaction.getMethod());
        assertEquals(PaymentStatus.UNPAID, transaction.getStatus());
    }
}
