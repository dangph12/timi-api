# Sepay & COD Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Integrate Sepay QR and COD payment methods into the order process, with real-time SSE notifications for QR payment success.

**Architecture:**
- **Entities:** Add `paymentMethod` and `paymentStatus` to `Order`. Add `PaymentTransaction` for audit logs.
- **Backend:** `SepayWebhookController` (HMAC validation), `SseController` (SSE push).
- **Frontend:** React client listens to SSE for payment updates.

**Tech Stack:** Java/Spring Boot, JPA/Hibernate, Spring Web SSE.

## Global Constraints
- **Validation:** Sepay webhooks MUST validate `X-SePay-Signature` (HMAC-SHA256).
- **Consistency:** `PaymentTransaction` must exist for all payments (COD, QR).
- **State Machine:** Keep `paymentStatus` and `workflowStatus` separate.

---

### Task 1: Model Updates

**Files:**
- Modify: `D:\(1)-Playground\timi-api\src\main\java\com\example\timi_api\domain\entity\Order.java`
- Create: `D:\(1)-Playground\timi-api\src\main\java\com\example\timi_api\domain\entity\PaymentTransaction.java`
- Create: `D:\(1)-Playground\timi-api\src\main\java\com\example\timi_api\domain\constant\PaymentMethod.java`
- Create: `D:\(1)-Playground\timi-api\src\main\java\com\example\timi_api\domain\constant\PaymentStatus.java`
- Modify: `D:\(1)-Playground\timi-api\src\main\java\com\example\timi_api\application\dto\request\CreateOrder.java`

**Interfaces:**
- Produces: `PaymentMethod` (QR, COD), `PaymentStatus` (UNPAID, PAID, FAILED, COD_PENDING).

- [ ] **Step 1: Create Enums**
- [ ] **Step 2: Update Order Entity**
- [ ] **Step 3: Create PaymentTransaction Entity**
- [ ] **Step 4: Update CreateOrder DTO**

### Task 2: Repository Setup

**Files:**
- Create: `D:\(1)-Playground\timi-api\src\main\java\com\example\timi_api\infrastructure\repository\PaymentTransactionRepository.java`

- [ ] **Step 1: Create Repository**

### Task 3: Sepay Webhook

**Files:**
- Create: `D:\(1)-Playground\timi-api\src\main\java\com\example\timi_api\infrastructure\web\v1\SepayWebhookController.java`
- Modify: `D:\(1)-Playground\timi-api\src\main\java\com\example\timi_api\application\service\OrderService.java`

- [ ] **Step 1: Implement Signature Validation**
- [ ] **Step 2: Implement Webhook Logic**
- [ ] **Step 3: Update OrderService for Payment Flow**

### Task 4: SSE Notification

**Files:**
- Create: `D:\(1)-Playground\timi-api\src\main\java\com\example\timi_api\infrastructure\web\v1\SseController.java`

- [ ] **Step 1: Implement SseController**
