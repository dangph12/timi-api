# Sepay Integration - Code Review Fixes

## Files Modified: 6

### 1. `OrderService.java` — Remove dead code
- Removed `processPayment()` method (unused — no callers in codebase)
- Removed unused imports: `PaymentCompletedEvent`, `ApplicationEventPublisher`, `BigDecimal`
- Removed unused fields: `paymentTransactionRepository`, `eventPublisher`

### 2. `SepayService.java` — Idempotency guard + logging
- Added `if (order.getPaymentStatus() == PaymentStatus.PAID) return;` in `handleCallback()` after fetching order
- Added `private static final Logger log = LoggerFactory.getLogger(SepayService.class)`
- Added `log.warn("Invalid Sepay signature")` when signature verification fails
- Added `log.error("Failed to process Sepay callback", e)` in catch block

### 3. `SseController.java` — SSE onError handler
- Added `emitter.onError(e -> emitters.remove(publicId))` after `onTimeout`

### 4. `SepayWebhookController.java` — Vietnamese error message
- Changed `"Invalid signature"` to `Message.INVALID_SIGNATURE`
- Added `import com.example.timi_api.infrastructure.message.Message`

### 5. `PaymentTransaction.java` — @PrePersist for createdAt
- Added `import jakarta.persistence.PrePersist`
- Added `prePersist()` method that sets `createdAt` if null before persist

### 6. `SepayWebhookControllerTest.java` — Updated test assertion
- Changed assertion to expect `Message.INVALID_SIGNATURE` instead of `"Invalid signature"`
- Added `import com.example.timi_api.infrastructure.message.Message`

## Tests
- 16/17 tests pass (the 1 failure — `TimiApiApplicationTests.contextLoads()` — is pre-existing, caused by missing DB config binding)
