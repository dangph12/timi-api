# Task 4 Report: SSE Notification

## Summary

Implemented Server-Sent Events (SSE) for real-time payment status push notifications to React frontend.

## Changes

### New Files

1. **`infrastructure/web/v1/SseController.java`**
   - `GET /api/sse/orders/{publicId}` — SSE subscribe endpoint (produces `text/event-stream`)
   - Validates order existence via `OrderRepository.findByPublicId()`
   - Stores emitters in `ConcurrentHashMap<String, SseEmitter>` (thread-safe)
   - `sendPaymentStatus(publicId, status)` — pushes `payment-status` event to subscriber
   - Cleanup: `onCompletion` / `onTimeout` remove emitter from map
   - Timeout set to `Long.MAX_VALUE` (no timeout)

2. **`infrastructure/event/PaymentEventListener.java`**
   - `@Component` + `@EventListener` listening for `PaymentCompletedEvent`
   - Calls `SseController.sendPaymentStatus()` with order's `publicId` and `PaymentStatus`

3. **`src/test/.../SseControllerTest.java`** — 5 tests covering subscribe, missing order, sendPaymentStatus paths
4. **`src/test/.../PaymentEventListenerTest.java`** — 1 test verifying event triggers SSE push

## Global Constraints Met

- `MediaType.TEXT_EVENT_STREAM_VALUE` on endpoint
- `Long.MAX_VALUE` timeout
- `ConcurrentHashMap` for thread safety
- `@EventListener` annotation (no polling)

## Test Results

Not run (user skipped execution). Code matches exact specification in task brief and follows project test patterns (JUnit 5 + MockitoExtension).
