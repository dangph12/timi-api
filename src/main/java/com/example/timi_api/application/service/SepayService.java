package com.example.timi_api.application.service;

import com.example.timi_api.domain.constant.PaymentMethod;
import com.example.timi_api.domain.constant.PaymentStatus;
import com.example.timi_api.domain.entity.Order;
import com.example.timi_api.domain.entity.PaymentTransaction;
import com.example.timi_api.domain.event.PaymentCompletedEvent;
import com.example.timi_api.infrastructure.message.Message;
import com.example.timi_api.infrastructure.repository.OrderRepository;
import com.example.timi_api.infrastructure.repository.PaymentTransactionRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SepayService {

    private static final Logger log = LoggerFactory.getLogger(SepayService.class);

    @Value("${sepay.secret-key}")
    private String secretKey;

    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public boolean verifySignature(String timestamp, String payload, String signature) {
        try {
            String data = timestamp + "." + payload;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] hmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hmac) {
                hexString.append(String.format("%02x", b));
            }
            boolean valid = ("sha256=" + hexString).equals(signature);
            if (!valid) {
                log.warn("Invalid Sepay signature");
            }
            return valid;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.warn("Invalid Sepay signature", e);
            return false;
        }
    }

    @Transactional
    public void handleCallback(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);

            String content = root.get("content").asText();
            BigDecimal amount = new BigDecimal(root.get("transferAmount").asText());
            String referenceCode = root.get("referenceCode").asText();

            Order order = orderRepository.findByPublicId(content)
                    .orElseThrow(() -> new NoSuchElementException(Message.NOT_FOUND));

            if (order.getPaymentStatus() == PaymentStatus.PAID) {
                return;
            }

            order.setPaymentMethod(PaymentMethod.QR);
            order.setPaymentStatus(PaymentStatus.PAID);
            orderRepository.save(order);

            PaymentTransaction transaction = PaymentTransaction.builder()
                    .order(order)
                    .amount(amount)
                    .method(PaymentMethod.QR)
                    .status(PaymentStatus.PAID)
                    .transactionReference(referenceCode)
                    .createdAt(LocalDateTime.now())
                    .build();
            paymentTransactionRepository.save(transaction);

            eventPublisher.publishEvent(new PaymentCompletedEvent(this, order));
        } catch (Exception e) {
            log.error("Failed to process Sepay callback", e);
            throw new RuntimeException("Failed to process Sepay callback", e);
        }
    }
}
