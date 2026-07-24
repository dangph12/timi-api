# Optimistic Locking & SKU Transaction History

## Overview

Add `@Version`-based optimistic locking to `Order`, `PaymentTransaction`, and `Sku` entities to prevent lost updates under concurrent access. Introduce `SkuTransaction` entity to maintain an immutable audit log of every SKU quantity change, linked back to the originating order where applicable.

## Architecture

- **Optimistic locking** via JPA `@Version` field. No application-level retry — conflict results in 409 Conflict returned to the caller.
- **SkuTransaction** logged in the same transactional boundary as the SKU quantity mutation. Two-phase cleanup not needed: if the transaction rolls back, the log rolls back too.
- No new dependencies. Pure JPA + Spring.

## Entities

### Changes to existing

| Entity | Change |
|--------|--------|
| `Order` | Add `@Version private Long version` |
| `PaymentTransaction` | Add `@Version private Long version` |
| `Sku` | Add `@Version private Long version` |

### New: `SkuTransaction`

| Field | Type | Nullable | Note |
|-------|------|----------|------|
| `id` | Long (PK, auto) | no | |
| `sku` | ManyToOne Sku | no | |
| `order` | ManyToOne Order | yes | null for MANUAL_ADJUST |
| `oldQuantity` | Integer | no | |
| `newQuantity` | Integer | no | |
| `changeAmount` | Integer | no | positive = increase, negative = decrease |
| `transactionType` | SkuTransactionType (enum) | no | ORDER_OUT, RESTOCK_IN, MANUAL_ADJUST |
| `createdAt` | LocalDateTime | no | `@PrePersist` set |

### New: `SkuTransactionType`

```java
public enum SkuTransactionType {
    ORDER_OUT,      // stock reduced when order placed
    RESTOCK_IN,     // stock returned when order cancelled
    MANUAL_ADJUST   // admin manual quantity adjustment
}
```

### New: `SkuTransactionRepository`

`extends JpaRepository<SkuTransaction, Long>`. No custom query methods needed.

## Service Changes

### `SkuService.adjustQuantity(Long skuId, int changeAmount, SkuTransactionType type, Order order)`

- `changeAmount` is delta: negative = reduce, positive = increase
- `order` nullable — only set for ORDER_OUT / RESTOCK_IN
- Loads Sku, validates new quantity >= 0, saves SkuTransaction + Sku
- `@Transactional`

### `OrderService.createOrder` (modify)

After `sku.setQuantity(...)` in the quantity-check loop (line 61):
```java
skuTransactionRepository.save(SkuTransaction.builder()
    .sku(sku)
    .order(order)
    .oldQuantity(sku.getQuantity() + item.getQuantity())
    .newQuantity(sku.getQuantity())
    .changeAmount(-item.getQuantity())
    .transactionType(SkuTransactionType.ORDER_OUT)
    .build());
```
Note: SkuTransaction save happens in the second loop (lines 86-100), after the `order` entity is persisted, so the `order` reference is valid.

### `OrderService.createOrderFromCartItems` (modify)

Same pattern as `createOrder`, slotted after `sku.setQuantity(...)` in the refactored flow that has the order saved first.

### `OrderService.cancelOrder` (modify)

Add SKU restock loop before status change:
```java
for (OrderItem item : order.getItems()) {
    Sku sku = item.getSku();
    int oldQty = sku.getQuantity();
    sku.setQuantity(oldQty + item.getQuantity());
    skuTransactionRepository.save(SkuTransaction.builder()
        .sku(sku)
        .order(order)
        .oldQuantity(oldQty)
        .newQuantity(sku.getQuantity())
        .changeAmount(item.getQuantity())
        .transactionType(SkuTransactionType.RESTOCK_IN)
        .build());
}
```

## Error Handling

New handler in `GlobalExceptionHandler`:
```java
@ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
@ResponseStatus(HttpStatus.CONFLICT)
public ApiResponse<Void> handleOptimisticLock(org.springframework.orm.ObjectOptimisticLockingFailureException ex) {
    return ApiResponse.failed("Dữ liệu đã bị thay đổi bởi người khác. Vui lòng thử lại.");
}
```

## Database Migrations

Flyway/Liquibase migration must:
1. Add `version BIGINT DEFAULT 0 NOT NULL` to `order`, `payment_transaction`, `sku`
2. Create `sku_transaction` table with columns matching the entity
3. Create `sku_transaction_type` enum if using native enum type (or rely on VARCHAR with `@Enumerated(STRING)`)

## Concurrency Scenario

| Scenario | Behavior |
|----------|----------|
| Two users order the same SKU simultaneously | One succeeds, one gets 409 on SKU version conflict — stock not oversold |
| Cancel happens while order is being modified | One wins, other gets 409 — consistent final state |
| Payment callback + manual status change race | Order `@Version` prevents silent overwrite |

## Scope Boundaries

- **In scope:** Entity changes, service changes, SkuTransaction creation, cancel restock, manual adjust method, 409 error handler
- **Out of scope:** REST endpoints for manual SKU adjust (admin controller), SkuTransaction query/listing endpoints, automatic retry logic, AOP-based transaction logging
