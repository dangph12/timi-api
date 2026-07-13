package com.example.timi_api.infrastructure.web.v1;

import com.example.timi_api.application.service.SepayPaymentService;
import com.example.timi_api.infrastructure.message.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final SepayPaymentService sepayPaymentService;

    @PostMapping("/sepay")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-SePay-Signature") String signature,
            @RequestHeader("X-SePay-Timestamp") String timestamp) {

        if (!sepayPaymentService.verifySignature(timestamp, payload, signature)) {
            return ResponseEntity.status(401).body(Message.INVALID_SIGNATURE);
        }

        sepayPaymentService.handleCallback(payload);
        return ResponseEntity.ok("OK");
    }
}
