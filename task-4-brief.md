# Task 4: SSE Notification

**Files:**
- Create: `D:\(1)-Playground\timi-api\src\main\java\com\example\timi_api\infrastructure\web\v1\SseController.java`
- Create: `D:\(1)-Playground\timi-api\src\main\java\com\example\timi_api\infrastructure\event\PaymentEventListener.java`

**Interfaces:**
- Consumes: `PaymentCompletedEvent` (from Task 3), `Order` entity
- Produces: SSE endpoint `/sse/orders/{publicId}`

## Steps

### Step 1: Create SseController

```java
package com.example.timi_api.infrastructure.web.v1;

import com.example.timi_api.domain.entity.Order;
import com.example.timi_api.infrastructure.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SseController {

    private final OrderRepository orderRepository;
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @GetMapping(value = "/orders/{publicId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable String publicId) {
        orderRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NoSuchElementException("Order not found"));

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(publicId, emitter);

        emitter.onCompletion(() -> emitters.remove(publicId));
        emitter.onTimeout(() -> emitters.remove(publicId));

        return emitter;
    }

    public void sendPaymentStatus(String publicId, String status) {
        SseEmitter emitter = emitters.get(publicId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("payment-status")
                        .data("{\"status\":\"" + status + "\"}"));
            } catch (Exception e) {
                emitters.remove(publicId);
            }
        }
    }
}
```

### Step 2: Create PaymentEventListener

```java
package com.example.timi_api.infrastructure.event;

import com.example.timi_api.domain.event.PaymentCompletedEvent;
import com.example.timi_api.infrastructure.web.v1.SseController;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final SseController sseController;

    @EventListener
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        sseController.sendPaymentStatus(
                event.getOrder().getPublicId(),
                event.getOrder().getPaymentStatus().name()
        );
    }
}
```

### Global Constraints
- SSE endpoint uses `MediaType.TEXT_EVENT_STREAM_VALUE`
- SseEmitter timeout set to `Long.MAX_VALUE` (no timeout)
- ConcurrentHashMap for thread-safe emitter storage
- Event listener uses `@EventListener` annotation (no polling)
