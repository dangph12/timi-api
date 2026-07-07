package com.example.timi_api.application.service;

import com.example.timi_api.application.dto.request.CreateOrder;
import com.example.timi_api.application.dto.request.CreateOrderItem;
import com.example.timi_api.domain.constant.OrderStatus;
import com.example.timi_api.domain.entity.*;
import com.example.timi_api.infrastructure.message.Message;
import com.example.timi_api.infrastructure.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final SkuRepository skuRepository;
    private final CharacterDesignRepository characterDesignRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public Order createOrder(CreateOrder request) {
        for (CreateOrderItem item : request.getItems()) {
            skuRepository.findById(item.getSkuId())
                    .orElseThrow(() -> new NoSuchElementException(Message.SKU_NOT_FOUND + item.getSkuId()));
            characterDesignRepository.findById(item.getCharacterDesignId())
                    .orElseThrow(() -> new NoSuchElementException(Message.DESIGN_NOT_FOUND + item.getCharacterDesignId()));
        }

        Account account = null;
        if (request.getAccountId() != null) {
            account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new NoSuchElementException(Message.ACCOUNT_NOT_FOUND));
        }

        Order order = orderRepository.save(Order.builder()
                .publicId(generatePublicId())
                .account(account)
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .currentStatus(OrderStatus.PENDING)
                .build());

        for (CreateOrderItem item : request.getItems()) {
            Sku sku = skuRepository.getReferenceById(item.getSkuId());
            CharacterDesign design = characterDesignRepository.getReferenceById(item.getCharacterDesignId());

            orderItemRepository.save(OrderItem.builder()
                    .order(order)
                    .sku(sku)
                    .characterDesign(design)
                    .quantity(item.getQuantity())
                    .priceAtPurchase(sku.getPrice())
                    .build());
        }

        orderStatusHistoryRepository.save(OrderStatusHistory.builder()
                .order(order)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build());

        return order;
    }

    private String generatePublicId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return timestamp + random;
    }
}
