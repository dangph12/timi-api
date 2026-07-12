package com.example.timi_api.domain.constant;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaymentStatusTest {
    @Test
    void testEnumValues() {
        assertNotNull(PaymentStatus.valueOf("UNPAID"));
        assertNotNull(PaymentStatus.valueOf("PAID"));
        assertNotNull(PaymentStatus.valueOf("FAILED"));
        assertNotNull(PaymentStatus.valueOf("COD_PENDING"));
    }
}
