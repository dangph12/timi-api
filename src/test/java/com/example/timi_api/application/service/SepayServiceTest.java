package com.example.timi_api.application.service;

import com.example.timi_api.domain.constant.PaymentMethod;
import com.example.timi_api.domain.constant.PaymentStatus;
import com.example.timi_api.domain.entity.Order;
import com.example.timi_api.domain.entity.PaymentTransaction;
import com.example.timi_api.domain.event.PaymentCompletedEvent;
import com.example.timi_api.infrastructure.repository.OrderRepository;
import com.example.timi_api.infrastructure.repository.PaymentTransactionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SepayServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private ObjectMapper objectMapper;

    private SepayService sepayService;

    @BeforeEach
    void setUp() {
        sepayService = new SepayService(orderRepository, paymentTransactionRepository, eventPublisher, objectMapper);
        ReflectionTestUtils.setField(sepayService, "secretKey", "test-secret-key");
    }

    @Test
    void verifySignature_validSignature_returnsTrue() {
        String payload = "test-payload";
        String expectedSignature = "d3f6248fa5891bbe7e77a7c4843b8ff5b270d6d11398fb9e5cfd4ad284e1a05b";
        assertTrue(sepayService.verifySignature(payload, expectedSignature));
    }

    @Test
    void verifySignature_invalidSignature_returnsFalse() {
        String payload = "test-payload";
        assertFalse(sepayService.verifySignature(payload, "invalid-signature"));
    }

    @Test
    void handleCallback_parsesPayloadAndProcessesPayment() throws Exception {
        String payload = "{\"code\":\"ORDER123\",\"transferAmount\":50000,\"referenceCode\":\"REF001\"}";

        JsonNode root = mock(JsonNode.class);
        when(root.get("code")).thenReturn(mock(JsonNode.class));
        when(root.get("code").asText()).thenReturn("ORDER123");
        when(root.get("transferAmount")).thenReturn(mock(JsonNode.class));
        when(root.get("transferAmount").asText()).thenReturn("50000");
        when(root.get("referenceCode")).thenReturn(mock(JsonNode.class));
        when(root.get("referenceCode").asText()).thenReturn("REF001");
        when(objectMapper.readTree(payload)).thenReturn(root);

        Order order = Order.builder().id(1L).publicId("ORDER123").paymentStatus(PaymentStatus.UNPAID).build();
        when(orderRepository.findByPublicId("ORDER123")).thenReturn(Optional.of(order));

        sepayService.handleCallback(payload);

        verify(orderRepository).findByPublicId("ORDER123");
        verify(orderRepository).save(order);
        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());

        ArgumentCaptor<PaymentTransaction> transactionCaptor = ArgumentCaptor.forClass(PaymentTransaction.class);
        verify(paymentTransactionRepository).save(transactionCaptor.capture());
        PaymentTransaction savedTransaction = transactionCaptor.getValue();
        assertEquals(order, savedTransaction.getOrder());
        assertEquals(new BigDecimal("50000"), savedTransaction.getAmount());
        assertEquals(PaymentMethod.QR, savedTransaction.getMethod());
        assertEquals(PaymentStatus.PAID, savedTransaction.getStatus());
        assertEquals("REF001", savedTransaction.getTransactionReference());
        assertNotNull(savedTransaction.getCreatedAt());

        verify(eventPublisher).publishEvent(any(PaymentCompletedEvent.class));
    }

    @Test
    void handleCallback_orderNotFound_throwsException() throws Exception {
        String payload = "{\"code\":\"NONEXISTENT\",\"transferAmount\":50000,\"referenceCode\":\"REF001\"}";

        JsonNode root = mock(JsonNode.class);
        when(root.get("code")).thenReturn(mock(JsonNode.class));
        when(root.get("code").asText()).thenReturn("NONEXISTENT");
        when(root.get("transferAmount")).thenReturn(mock(JsonNode.class));
        when(root.get("transferAmount").asText()).thenReturn("50000");
        when(root.get("referenceCode")).thenReturn(mock(JsonNode.class));
        when(root.get("referenceCode").asText()).thenReturn("REF001");
        when(objectMapper.readTree(payload)).thenReturn(root);

        when(orderRepository.findByPublicId("NONEXISTENT")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> sepayService.handleCallback(payload));
        verify(orderRepository).findByPublicId("NONEXISTENT");
        verify(paymentTransactionRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}
