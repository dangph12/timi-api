package com.example.timi_api.infrastructure.web.v1;

import com.example.timi_api.application.service.SepayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SepayWebhookControllerTest {

    @Mock
    private SepayService sepayService;

    private SepayWebhookController controller;

    @BeforeEach
    void setUp() {
        controller = new SepayWebhookController(sepayService);
    }

    @Test
    void handleWebhook_validSignature_returnsOk() {
        when(sepayService.verifySignature(anyString(), anyString())).thenReturn(true);

        var response = controller.handleWebhook("{\"code\":\"ORDER123\"}", "valid-sig");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("OK", response.getBody());
        verify(sepayService).handleCallback(anyString());
    }

    @Test
    void handleWebhook_invalidSignature_returnsUnauthorized() {
        when(sepayService.verifySignature(anyString(), anyString())).thenReturn(false);

        var response = controller.handleWebhook("{\"code\":\"ORDER123\"}", "bad-sig");

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Invalid signature", response.getBody());
        verify(sepayService, never()).handleCallback(anyString());
    }
}
