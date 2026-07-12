package com.example.timi_api.domain.constant;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaymentMethodTest {
    @Test
    void testEnumValues() {
        assertNotNull(PaymentMethod.valueOf("QR"));
        assertNotNull(PaymentMethod.valueOf("COD"));
    }
}
