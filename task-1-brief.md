# Task 1: Model Updates

**Files:**
- Modify: `D:\(1)-Playground\timi-api\src\main\java\com\example\timi_api\domain\entity\Order.java`
- Create: `D:\(1)-Playground\timi-api\src\main\java\com\example\timi_api\domain\entity\PaymentTransaction.java`
- Create: `D:\(1)-Playground\timi-api\src\main\java\com\example\timi_api\domain\constant\PaymentMethod.java`
- Create: `D:\(1)-Playground\timi-api\src\main\java\com\example\timi_api\domain\constant\PaymentStatus.java`
- Modify: `D:\(1)-Playground\timi-api\src\main\java\com\example\timi_api\application\dto\request\CreateOrder.java`

**Interfaces:**
- Produces: `PaymentMethod` (QR, COD), `PaymentStatus` (UNPAID, PAID, FAILED, COD_PENDING).

## Steps

### Step 1: Create Enums

Create `PaymentMethod.java`:
```java
package com.example.timi_api.domain.constant;

public enum PaymentMethod {
    QR, COD
}
```

Create `PaymentStatus.java`:
```java
package com.example.timi_api.domain.constant;

public enum PaymentStatus {
    UNPAID, PAID, FAILED, COD_PENDING
}
```

### Step 2: Update Order Entity

Current `Order.java` fields: id, publicId, account, email, name, phone, address, note, currentStatus, items, statusHistory.

Add:
- `@Enumerated(EnumType.STRING) private PaymentMethod paymentMethod;`
- `@Enumerated(EnumType.STRING) private PaymentStatus paymentStatus;`

Update `@Builder` to handle defaults if needed.

### Step 3: Create PaymentTransaction Entity

```java
package com.example.timi_api.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment_transaction")
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Order order;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    private String transactionReference;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
```

### Step 4: Update CreateOrder DTO

Add to `CreateOrder.java`:
- `private PaymentMethod paymentMethod;` (nullable)

### Global Constraints
- Validation: Sepay webhooks MUST validate X-SePay-Signature (HMAC-SHA256).
- Consistency: PaymentTransaction must exist for all payments (COD, QR).
- State Machine: Keep paymentStatus and workflowStatus separate.
