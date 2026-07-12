package com.example.timi_api.infrastructure.web.v1;

import com.example.timi_api.application.service.SepayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class SepayWebhookController {

    private final SepayService sepayService;

    @PostMapping("/sepay")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-SePay-Signature") String signature) {

        if (!sepayService.verifySignature(payload, signature)) {
            return ResponseEntity.status(401).body("Invalid signature");
        }

        sepayService.handleCallback(payload);
        return ResponseEntity.ok("OK");
    }
}
