# Task 2: Repository Setup

**Files:**
- Create: `D:\(1)-Playground\timi-api\src\main\java\com\example\timi_api\infrastructure\repository\PaymentTransactionRepository.java`

**Interfaces:**
- Consumes: `PaymentTransaction` entity (from Task 1)
- Produces: `PaymentTransactionRepository` (Spring Data JPA interface extending JpaRepository)

## Steps

### Step 1: Create Repository

```java
package com.example.timi_api.infrastructure.repository;

import com.example.timi_api.domain.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    List<PaymentTransaction> findByOrderId(Long orderId);
}
```

### Global Constraints
- Follow existing repository patterns in `infrastructure/repository/` (e.g., `OrderRepository.java`, `AccountRepository.java`)
- Use `@Repository` annotation
