package com.example.timi_api.infrastructure.web.v1;

import com.example.timi_api.application.service.SepayService;
import com.example.timi_api.infrastructure.message.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class SepayWebhookController {

    private final SepayService sepayService;

    @PostMapping("/sepay")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-SePay-Signature") String signature,
            @RequestHeader("X-SePay-Timestamp") String timestamp) {

        if (!sepayService.verifySignature(timestamp, payload, signature)) {
            return ResponseEntity.status(401).body(Message.INVALID_SIGNATURE);
        }

        sepayService.handleCallback(payload);
        return ResponseEntity.ok("OK");
    }
}
