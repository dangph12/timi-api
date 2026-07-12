# Task 3: Sepay Webhook

**Files:**
- Create: `D:\(1)-Playground\timi-api\src\main\java\com\example\timi_api\infrastructure\web\v1\SepayWebhookController.java`
- Modify: `D:\(1)-Playground\timi-api\src\main\java\com\example\timi_api\application\service\OrderService.java`
- Create: `D:\(1)-Playground\timi-api\src\main\java\com\example\timi_api\application\service\SepayService.java`
- Create: `D:\(1)-Playground\timi-api\src\main\java\com\example\timi_api\domain\event\PaymentCompletedEvent.java`

**Interfaces:**
- Consumes: `PaymentMethod`, `PaymentStatus` enums, `PaymentTransaction`, `Order` entity, `PaymentTransactionRepository`
- Produces: `SepayWebhookController` (POST endpoint), `SepayService` (signature validation + order update), `PaymentCompletedEvent` (event for SSE)

## Steps

### Step 1: Create PaymentCompletedEvent

```java
package com.example.timi_api.domain.event;

import com.example.timi_api.domain.entity.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PaymentCompletedEvent extends ApplicationEvent {

    private final Order order;

    public PaymentCompletedEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
}
```

### Step 2: Update OrderService

Add to `OrderService`:

```java
import com.example.timi_api.domain.constant.PaymentMethod;
import com.example.timi_api.domain.constant.PaymentStatus;
import com.example.timi_api.domain.entity.PaymentTransaction;
import com.example.timi_api.domain.event.PaymentCompletedEvent;
import com.example.timi_api.infrastructure.repository.PaymentTransactionRepository;
import org.springframework.context.ApplicationEventPublisher;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// Inject:
// private final PaymentTransactionRepository paymentTransactionRepository;
// private final ApplicationEventPublisher eventPublisher;

// Update createOrder to set paymentMethod and paymentStatus from request
// In createOrder, after building Order:
// .paymentMethod(request.getPaymentMethod())
// .paymentStatus(PaymentStatus.UNPAID)

// Add new method:
@Transactional
public void processPayment(Long orderId, String transactionReference, BigDecimal amount) {
    Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NoSuchElementException(Message.NOT_FOUND));

    order.setPaymentStatus(PaymentStatus.PAID);
    orderRepository.save(order);

    PaymentTransaction transaction = PaymentTransaction.builder()
            .order(order)
            .amount(amount)
            .method(PaymentMethod.QR)
            .status(PaymentStatus.PAID)
            .transactionReference(transactionReference)
            .createdAt(LocalDateTime.now())
            .build();
    paymentTransactionRepository.save(transaction);

    eventPublisher.publishEvent(new PaymentCompletedEvent(this, order));
}
```

### Step 3: Create SepayService

```java
package com.example.timi_api.application.service;

import com.example.timi_api.domain.entity.Order;
import com.example.timi_api.domain.event.PaymentCompletedEvent;
import com.example.timi_api.infrastructure.repository.PaymentTransactionRepository;
import com.example.timi_api.infrastructure.repository.OrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class SepayService {

    @Value("${sepay.secret-key}")
    private String secretKey;

    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public boolean verifySignature(String payload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            mac.init(keySpec);
            byte[] hmac = mac.doFinal(payload.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hmac) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString().equals(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return false;
        }
    }

    @Transactional
    public void handleCallback(String payload) {
        // Parse JSON payload
        // Expected fields: id, gateway, transactionDate, accountNumber, code, transferAmount, referenceCode
        // "code" field contains the order reference (publicId)
        // Implementation depends on actual Sepay schema
    }
}
```

### Step 4: Create SepayWebhookController

```java
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
```

### Step 5: Add application properties

Add to `application.properties` or `application.yml`:
```properties
sepay.secret-key=${SEPAY_SECRET_KEY:}
```

### Step 6: Add messages to Message.java

Add to `Message.java`:
```java
public static final String PAYMENT_SUCCESS = "Thanh toán thành công";
public static final String INVALID_SIGNATURE = "Chữ ký không hợp lệ";
```

### Global Constraints
- Validation: Sepay webhooks MUST validate X-SePay-Signature (HMAC-SHA256).
- Consistency: PaymentTransaction must exist for all payments.
- State Machine: Keep paymentStatus and workflowStatus separate.
- Secret key NEVER hardcoded - use environment variable `SEPAY_SECRET_KEY`.
