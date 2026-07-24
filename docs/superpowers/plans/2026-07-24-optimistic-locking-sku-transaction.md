# Optimistic Locking & SKU Transaction History Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add `@Version` optimistic locking to Order, PaymentTransaction, Sku and introduce SkuTransaction audit log for all SKU quantity changes.

**Architecture:** JPA `@Version` on 3 entities; new SkuTransaction entity with optional Order link; SkuTransaction saved in same transactional boundary as the SKU mutation; conflict returns 409.

**Tech Stack:** Spring Boot 4.1.0, JPA/Hibernate, PostgreSQL, Lombok, Gradle (Kotlin DSL), Java 21

## Global Constraints

- Spring Boot 4.1.0, Java 21
- Project structure: domain/entity, domain/constant, infrastructure/repository, application/service, infrastructure/common
- All entities use Lombok `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- Enums stored as VARCHAR via `@Enumerated(EnumType.STRING)`
- ddl-auto: validate — DDL changes are manual SQL
- Error responses use `ApiResponse.failed(message)`
- No comments unless explicitly requested

---

### Task 1: Add `@Version` to Order, PaymentTransaction, Sku

**Files:**
- Modify: `src/main/java/com/example/timi_api/domain/entity/Order.java`
- Modify: `src/main/java/com/example/timi_api/domain/entity/PaymentTransaction.java`
- Modify: `src/main/java/com/example/timi_api/domain/entity/Sku.java`

**Interfaces:**
- Produces: `Order.version: Long`, `PaymentTransaction.version: Long`, `Sku.version: Long` — managed by JPA, no direct consumer calls

- [ ] **Step 1: Add version field to Sku.java**

After line 41 (`private Integer quantity;`), add:
```java
@Version
private Long version;
```

- [ ] **Step 2: Add version field to Order.java**

After line 30 (`private String idempotencyKey;`), add:
```java
@Version
private Long version;
```

- [ ] **Step 3: Add version field to PaymentTransaction.java**

After line 44 (`private String idempotencyKey;`), add:
```java
@Version
private Long version;
```

- [ ] **Step 4: Verify compilation**

Run: `./gradlew compileJava -x test`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/example/timi_api/domain/entity/Sku.java src/main/java/com/example/timi_api/domain/entity/Order.java src/main/java/com/example/timi_api/domain/entity/PaymentTransaction.java
git commit -m "feat: add @Version optimistic locking to Order, PaymentTransaction, Sku"
```

---

### Task 2: Create `SkuTransactionType` enum

**Files:**
- Create: `src/main/java/com/example/timi_api/domain/constant/SkuTransactionType.java`

**Interfaces:**
- Produces: `SkuTransactionType.ORDER_OUT`, `SkuTransactionType.RESTOCK_IN`, `SkuTransactionType.MANUAL_ADJUST`

- [ ] **Step 1: Create SkuTransactionType.java**

```java
package com.example.timi_api.domain.constant;

public enum SkuTransactionType {
    ORDER_OUT,
    RESTOCK_IN,
    MANUAL_ADJUST
}
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew compileJava -x test`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/example/timi_api/domain/constant/SkuTransactionType.java
git commit -m "feat: add SkuTransactionType enum"
```

---

### Task 3: Create `SkuTransaction` entity + repository

**Files:**
- Create: `src/main/java/com/example/timi_api/domain/entity/SkuTransaction.java`
- Create: `src/main/java/com/example/timi_api/infrastructure/repository/SkuTransactionRepository.java`

**Interfaces:**
- Produces: `SkuTransaction` entity with fields `id: Long`, `sku: Sku`, `order: Order`, `oldQuantity: Integer`, `newQuantity: Integer`, `changeAmount: Integer`, `transactionType: SkuTransactionType`, `createdAt: LocalDateTime`
- Produces: `SkuTransactionRepository extends JpaRepository<SkuTransaction, Long>` — no custom methods

- [ ] **Step 1: Create SkuTransaction entity**

```java
package com.example.timi_api.domain.entity;

import com.example.timi_api.domain.constant.SkuTransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sku_transaction")
public class SkuTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sku_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Sku sku;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Order order;

    @Column(nullable = false)
    private Integer oldQuantity;

    @Column(nullable = false)
    private Integer newQuantity;

    @Column(nullable = false)
    private Integer changeAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkuTransactionType transactionType;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
```

- [ ] **Step 2: Create SkuTransactionRepository**

```java
package com.example.timi_api.infrastructure.repository;

import com.example.timi_api.domain.entity.SkuTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkuTransactionRepository extends JpaRepository<SkuTransaction, Long> {
}
```

- [ ] **Step 3: Verify compilation**

Run: `./gradlew compileJava -x test`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/example/timi_api/domain/entity/SkuTransaction.java src/main/java/com/example/timi_api/infrastructure/repository/SkuTransactionRepository.java
git commit -m "feat: add SkuTransaction entity and repository"
```

---

### Task 4: Handle `ObjectOptimisticLockingFailureException` in `GlobalExceptionHandler`

**Files:**
- Modify: `src/main/java/com/example/timi_api/infrastructure/common/GlobalExceptionHandler.java`

**Interfaces:**
- Produces: 409 Conflict response when optimistic lock fails

- [ ] **Step 1: Add exception handler**

After the `handleAuthentication` method (line 54), add a new method before the closing `}` of the class:

```java
@ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
@ResponseStatus(HttpStatus.CONFLICT)
public ApiResponse<Void> handleOptimisticLock(org.springframework.orm.ObjectOptimisticLockingFailureException ex) {
    return ApiResponse.failed("Dữ liệu đã bị thay đổi bởi người khác. Vui lòng thử lại.");
}
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew compileJava -x test`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/example/timi_api/infrastructure/common/GlobalExceptionHandler.java
git commit -m "feat: handle ObjectOptimisticLockingFailureException with 409 Conflict"
```

---

### Task 5: Add `adjustQuantity` to `SkuService`

**Files:**
- Modify: `src/main/java/com/example/timi_api/application/service/SkuService.java`

**Interfaces:**
- Produces: `public void adjustQuantity(Long skuId, int changeAmount, SkuTransactionType type, Order order)`
- Consumes: `SkuTransactionRepository`, `SkuTransaction` entity, `SkuTransactionType`

- [ ] **Step 1: Add imports and repository field to SkuService**

Add these imports (after existing imports):
```java
import com.example.timi_api.domain.constant.SkuTransactionType;
import com.example.timi_api.domain.entity.Order;
import com.example.timi_api.domain.entity.Sku;
import com.example.timi_api.domain.entity.SkuTransaction;
import com.example.timi_api.infrastructure.repository.SkuTransactionRepository;
```

Add repository field (after `private final SkuRepository skuRepository;`):
```java
private final SkuTransactionRepository skuTransactionRepository;
```

- [ ] **Step 2: Add adjustQuantity method**

At the end of the class (before closing `}`), add:
```java
@Transactional
public void adjustQuantity(Long skuId, int changeAmount, SkuTransactionType type, Order order) {
    Sku sku = skuRepository.findById(skuId)
            .orElseThrow(() -> new java.util.NoSuchElementException("Không tìm thấy SKU: " + skuId));

    int oldQuantity = sku.getQuantity();
    int newQuantity = oldQuantity + changeAmount;

    if (newQuantity < 0) {
        throw new IllegalArgumentException("Số lượng hàng trong kho không đủ");
    }

    sku.setQuantity(newQuantity);

    skuTransactionRepository.save(SkuTransaction.builder()
            .sku(sku)
            .order(order)
            .oldQuantity(oldQuantity)
            .newQuantity(newQuantity)
            .changeAmount(changeAmount)
            .transactionType(type)
            .build());

    skuRepository.save(sku);
}
```

- [ ] **Step 3: Verify compilation**

Run: `./gradlew compileJava -x test`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/example/timi_api/application/service/SkuService.java
git commit -m "feat: add adjustQuantity with SkuTransaction logging to SkuService"
```

---

### Task 6: Modify `OrderService.createOrder` to log SkuTransaction

**Files:**
- Modify: `src/main/java/com/example/timi_api/application/service/OrderService.java:86-100`

**Interfaces:**
- Consumes: `SkuTransactionRepository`, `SkuTransaction`, `SkuTransactionType`
- Produces: SkuTransaction(ORDER_OUT) saved per item in second loop

- [ ] **Step 1: Add imports to OrderService**

Add these imports:
```java
import com.example.timi_api.domain.constant.SkuTransactionType;
import com.example.timi_api.domain.entity.SkuTransaction;
import com.example.timi_api.infrastructure.repository.SkuTransactionRepository;
```

- [ ] **Step 2: Add SkuTransactionRepository field**

After line 38 (`private final SkuRepository skuRepository;`), add:
```java
private final SkuTransactionRepository skuTransactionRepository;
```

- [ ] **Step 3: Insert SkuTransaction save in second loop**

In the second loop of `createOrder` (lines 86-91), after getting `Sku sku` and before computing price, add SkuTransaction save. Replace lines 87-91:

Old (lines 86-91):
```java
        for (CreateOrderItem item : request.getItems()) {
            Sku sku = skuRepository.getReferenceById(item.getSkuId());
            CharacterDesign design = characterDesignRepository.getReferenceById(item.getCharacterDesignId());
            BigDecimal price = sku.getPrice();
            total = total.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));
```

New:
```java
        for (CreateOrderItem item : request.getItems()) {
            Sku sku = skuRepository.getReferenceById(item.getSkuId());
            CharacterDesign design = characterDesignRepository.getReferenceById(item.getCharacterDesignId());

            skuTransactionRepository.save(SkuTransaction.builder()
                    .sku(sku)
                    .order(order)
                    .oldQuantity(sku.getQuantity() + item.getQuantity())
                    .newQuantity(sku.getQuantity())
                    .changeAmount(-item.getQuantity())
                    .transactionType(SkuTransactionType.ORDER_OUT)
                    .build());

            BigDecimal price = sku.getPrice();
            total = total.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));
```

- [ ] **Step 4: Verify compilation**

Run: `./gradlew compileJava -x test`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/example/timi_api/application/service/OrderService.java
git commit -m "feat: log SkuTransaction(ORDER_OUT) in createOrder"
```

---

### Task 7: Modify `OrderService.createOrderFromCartItems` to log SkuTransaction

**Files:**
- Modify: `src/main/java/com/example/timi_api/application/service/OrderService.java:254-268`

**Interfaces:**
- Consumes: `SkuTransactionRepository`, `SkuTransaction`, `SkuTransactionType` (already imported from Task 6)
- Produces: SkuTransaction(ORDER_OUT) saved per item

- [ ] **Step 1: Insert SkuTransaction save in cart items loop**

In `createOrderFromCartItems`, in the second loop (lines 254-258), after `Sku sku = skus.get(i)` and before `CharacterDesign design = ...`, add SkuTransaction save. Replace lines 254-258:

Old (lines 254-258):
```java
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem cartItem = cartItems.get(i);
            Sku sku = skus.get(i);
            CharacterDesign design = characterDesignRepository.getReferenceById(cartItem.getCharacterDesign().getId());
            BigDecimal price = sku.getPrice();
```

New:
```java
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem cartItem = cartItems.get(i);
            Sku sku = skus.get(i);
            CharacterDesign design = characterDesignRepository.getReferenceById(cartItem.getCharacterDesign().getId());

            skuTransactionRepository.save(SkuTransaction.builder()
                    .sku(sku)
                    .order(order)
                    .oldQuantity(sku.getQuantity() + cartItem.getQuantity())
                    .newQuantity(sku.getQuantity())
                    .changeAmount(-cartItem.getQuantity())
                    .transactionType(SkuTransactionType.ORDER_OUT)
                    .build());

            BigDecimal price = sku.getPrice();
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew compileJava -x test`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/example/timi_api/application/service/OrderService.java
git commit -m "feat: log SkuTransaction(ORDER_OUT) in createOrderFromCartItems"
```

---

### Task 8: Modify `OrderService.cancelOrder` to restock SKUs + log SkuTransaction

**Files:**
- Modify: `src/main/java/com/example/timi_api/application/service/OrderService.java:197-218`

**Interfaces:**
- Consumes: `SkuTransactionRepository`, `SkuTransaction`, `SkuTransactionType` (already imported)
- Produces: SKU restock + SkuTransaction(RESTOCK_IN) per item on cancel

- [ ] **Step 1: Add restock loop in cancelOrder**

In `cancelOrder` method, after validating order can be cancelled (line 204) and before the status change (line 207), add the restock loop. Replace lines 206-208:

Old (lines 206-208):
```java
        order.setCurrentStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
```

New:
```java
        for (OrderItem item : order.getItems()) {
            Sku sku = item.getSku();
            int oldQuantity = sku.getQuantity();
            int newQuantity = oldQuantity + item.getQuantity();
            sku.setQuantity(newQuantity);
            skuRepository.save(sku);

            skuTransactionRepository.save(SkuTransaction.builder()
                    .sku(sku)
                    .order(order)
                    .oldQuantity(oldQuantity)
                    .newQuantity(newQuantity)
                    .changeAmount(item.getQuantity())
                    .transactionType(SkuTransactionType.RESTOCK_IN)
                    .build());
        }

        order.setCurrentStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew compileJava -x test`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/example/timi_api/application/service/OrderService.java
git commit -m "feat: restock SKUs and log SkuTransaction(RESTOCK_IN) on order cancel"
```

---

### Task 9: Write DDL migration script

**Files:**
- Create: `src/main/resources/db/migration/2026-07-24-optimistic-locking-sku-transaction.sql`

**Interfaces:**
- Produces: SQL scripts for manual schema update (ddl-auto is validate, no Flyway)

- [ ] **Step 1: Create migration directory**

```bash
New-Item -ItemType Directory -Path src/main/resources/db/migration -Force
```

- [ ] **Step 2: Write migration SQL**

```sql
-- Add @Version columns for optimistic locking
ALTER TABLE "order" ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL;
ALTER TABLE payment_transaction ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL;
ALTER TABLE sku ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL;

-- Create SKU transaction audit table
CREATE TABLE IF NOT EXISTS sku_transaction (
    id BIGSERIAL PRIMARY KEY,
    sku_id BIGINT NOT NULL REFERENCES sku(id),
    order_id BIGINT REFERENCES "order"(id),
    old_quantity INTEGER NOT NULL,
    new_quantity INTEGER NOT NULL,
    change_amount INTEGER NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sku_transaction_sku_id ON sku_transaction(sku_id);
CREATE INDEX IF NOT EXISTS idx_sku_transaction_order_id ON sku_transaction(order_id);
```

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/db/migration/2026-07-24-optimistic-locking-sku-transaction.sql
git commit -m "db: add migration for version columns and sku_transaction table"
```
