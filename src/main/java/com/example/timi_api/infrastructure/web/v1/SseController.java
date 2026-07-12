package com.example.timi_api.infrastructure.web.v1;

import com.example.timi_api.infrastructure.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/sse")
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
        emitter.onError(e -> emitters.remove(publicId));

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
