# Idempotency Keys + Payment Transaction Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** True idempotency for order creation, COD confirm, and QR webhook via client keys, DB constraints, and row locks.

**Architecture:** New nullable unique `idempotency_key` column on `order` and `payment_transaction`. Client sends `Idempotency-Key` header; server replays stored result on duplicate. Pessimistic row lock on order closes TOCTOU races. QR keeps `transactionReference` as provider key and gains a pre-check; the broken in-transaction exception catch is removed.

**Tech Stack:** Spring Boot 4.1, JPA/Hibernate (Postgres, `ddl-auto: validate`), Lombok.

## Global Constraints

- Schema managed manually (no migration tool). DDL applied by hand BEFORE deploy, else `validate` fails boot.
- `Idempotency-Key` header optional everywhere = backward compatible.
- No test cases (user decision). Verification = `gradlew compileJava` per task + manual smoke.
- Accepted tradeoffs: key reuse with different body returns original order (no 409); no key TTL/cleanup; frontend generates UUID per checkout attempt, regenerates after success.

---

### Task 1: Manual DDL

**Files:**
- Create: `docs/sql/2026-07-15-idempotency-keys.sql`

```sql
ALTER TABLE "order" ADD COLUMN idempotency_key VARCHAR(64);
CREATE UNIQUE INDEX uq_order_idempotency_key ON "order" (idempotency_key);

ALTER TABLE payment_transaction ADD COLUMN idempotency_key VARCHAR(64);
CREATE UNIQUE INDEX uq_payment_tx_idempotency_key ON payment_transaction (idempotency_key);

-- backstop verify: ensure reference unique index actually exists
CREATE UNIQUE INDEX IF NOT EXISTS uq_payment_tx_reference ON payment_transaction (transaction_reference);

-- one PENDING transaction per order, DB-level backstop for COD race
CREATE UNIQUE INDEX uq_payment_tx_pending_per_order
    ON payment_transaction (order_id) WHERE status = 'PENDING';
```

Pre-check before running partial index:

```sql
SELECT order_id FROM payment_transaction WHERE status = 'PENDING' GROUP BY order_id HAVING count(*) > 1;
```

Existing duplicates block index creation; clean first.

- [ ] Save file; user runs against DB manually
- [ ] Commit: `docs: add idempotency key DDL`

### Task 2: Entities + repositories

**Files:**
- Modify: `src/main/java/com/example/timi_api/domain/entity/Order.java`
- Modify: `src/main/java/com/example/timi_api/domain/entity/PaymentTransaction.java`
- Modify: `src/main/java/com/example/timi_api/infrastructure/repository/OrderRepository.java`
- Modify: `src/main/java/com/example/timi_api/infrastructure/repository/PaymentTransactionRepository.java`

**Interfaces produced:**
- `Order.idempotencyKey`, `PaymentTransaction.idempotencyKey` (`@Column(unique = true) String`)
- `OrderRepository.findByIdempotencyKey(String)`, `OrderRepository.findByPublicIdForUpdate(String)` (PESSIMISTIC_WRITE)
- `PaymentTransactionRepository.existsByTransactionReference(String)`, `findByIdempotencyKey(String)`

```java
// Order.java + PaymentTransaction.java — add field
@Column(unique = true)
private String idempotencyKey;
```

```java
// OrderRepository.java
Optional<Order> findByIdempotencyKey(String idempotencyKey);

@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select o from Order o where o.publicId = :publicId")
Optional<Order> findByPublicIdForUpdate(@Param("publicId") String publicId);
```

```java
// PaymentTransactionRepository.java
boolean existsByTransactionReference(String transactionReference);
Optional<PaymentTransaction> findByIdempotencyKey(String idempotencyKey);
```

- [ ] Verify: `gradlew compileJava`
- [ ] Commit: `feat: add idempotency key fields and repository methods`

### Task 3: QR webhook fixes

**Files:**
- Modify: `src/main/java/com/example/timi_api/application/service/SepayPaymentService.java`

- [ ] `findByPublicId` -> `findByPublicIdForUpdate`
- [ ] After PAID check: `if (paymentTransactionRepository.existsByTransactionReference(referenceCode)) return;`
- [ ] `!amount.equals(...)` -> `amount.compareTo(order.getTotalAmount()) != 0`
- [ ] Delete `catch (DataIntegrityViolationException)` block (dead code — violation fires at commit, outside method; constraint stays as backstop, Sepay retry then hits pre-check)
- [ ] Verify: `gradlew compileJava`
- [ ] Commit: `fix: harden Sepay webhook idempotency and amount comparison`

### Task 4: createOrder idempotency

**Files:**
- Modify: `src/main/java/com/example/timi_api/application/service/OrderService.java`
- Modify: `src/main/java/com/example/timi_api/infrastructure/web/v1/OrderController.java`

- [ ] Service signature `createOrder(CreateOrder request, String idempotencyKey)`; first lines:

```java
if (idempotencyKey != null) {
    Optional<Order> existing = orderRepository.findByIdempotencyKey(idempotencyKey);
    if (existing.isPresent()) {
        return toOrderResponse(existing.get());
    }
}
```

- [ ] Builder gets `.idempotencyKey(idempotencyKey)`
- [ ] Add `public OrderResponse getOrderByIdempotencyKey(String key)`
- [ ] Controller — race backstop OUTSIDE transaction:

```java
@PostMapping
public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
        @Valid @RequestBody CreateOrder request,
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
    OrderResponse order;
    try {
        order = orderService.createOrder(request, idempotencyKey);
    } catch (DataIntegrityViolationException e) {
        if (idempotencyKey == null) throw e;
        order = orderService.getOrderByIdempotencyKey(idempotencyKey);
    }
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(Message.ORDER_CREATED, order));
}
```

Replay = rollback = no duplicate confirmation email (afterCommit sync never fires).

- [ ] Verify: `gradlew compileJava`
- [ ] Commit: `feat: idempotent order creation via Idempotency-Key header`

### Task 5: COD idempotency

**Files:**
- Modify: `src/main/java/com/example/timi_api/application/service/OrderService.java`
- Modify: `src/main/java/com/example/timi_api/infrastructure/web/v1/OrderController.java`

- [ ] Service `selectCodPayment(String publicId, String idempotencyKey)`:
  - `findByPublicId` -> `findByPublicIdForUpdate`
  - Guard: `if (order.getCurrentPaymentStatus() == PaymentStatus.PAID) throw new IllegalArgumentException("Đơn hàng đã được thanh toán");`
  - Before PENDING check: key non-null + `paymentTransactionRepository.findByIdempotencyKey(key)` present -> `return toOrderResponse(order);`
  - Builder gets `.idempotencyKey(idempotencyKey)`
- [ ] Controller `confirmCodPayment`: optional `Idempotency-Key` header; catch `DataIntegrityViolationException` -> key present: refetch by key path (`orderService.getOrderByPublicId(publicId)` returns same order state); key null (race hit partial index): `return orderService.getOrderByPublicId(publicId);`
- [ ] Verify: `gradlew compileJava`
- [ ] Commit: `feat: idempotent COD confirmation with PAID guard`

---

**Frontend contract:** header `Idempotency-Key: <uuid>` on `POST /v1/orders` and `POST /v1/orders/{publicId}/confirm-payment`; regenerate after success.
