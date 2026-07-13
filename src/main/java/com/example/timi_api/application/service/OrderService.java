package com.example.timi_api.application.service;

import com.example.timi_api.application.dto.request.CreateOrder;
import com.example.timi_api.application.dto.request.CreateOrderItem;
import com.example.timi_api.application.dto.response.CharacterDesignResponse;
import com.example.timi_api.application.dto.response.OrderItemResponse;
import com.example.timi_api.application.dto.response.OrderResponse;
import com.example.timi_api.application.dto.response.OrderStatusHistoryResponse;
import com.example.timi_api.application.dto.response.SkuResponse;
import com.example.timi_api.domain.constant.OrderStatus;
import com.example.timi_api.domain.constant.PaymentMethod;
import com.example.timi_api.domain.constant.PaymentStatus;
import com.example.timi_api.domain.entity.*;
import com.example.timi_api.infrastructure.email.EmailService;
import com.example.timi_api.infrastructure.message.Message;
import com.example.timi_api.infrastructure.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final EmailService emailService;

    @Transactional
    public OrderResponse createOrder(CreateOrder request) {
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

        OrderStatus initialStatus = OrderStatus.CREATED;

        Order order = orderRepository.save(Order.builder()
                .publicId(generatePublicId())
                .account(account)
                .email(request.getEmail())
                .name(request.getName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .note(request.getNote())
                .currentStatus(initialStatus)
                .currentPaymentStatus(PaymentStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build());

        BigDecimal total = BigDecimal.ZERO;
        for (CreateOrderItem item : request.getItems()) {
            Sku sku = skuRepository.getReferenceById(item.getSkuId());
            CharacterDesign design = characterDesignRepository.getReferenceById(item.getCharacterDesignId());
            BigDecimal price = sku.getPrice();
            total = total.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));

            OrderItem savedItem = orderItemRepository.save(OrderItem.builder()
                    .order(order)
                    .sku(sku)
                    .characterDesign(design)
                    .quantity(item.getQuantity())
                    .priceAtPurchase(price)
                    .build());
            order.getItems().add(savedItem);
        }

        order.setTotalAmount(total);
        orderRepository.save(order);

        orderStatusHistoryRepository.save(OrderStatusHistory.builder()
                .order(order)
                .status(initialStatus)
                .createdAt(LocalDateTime.now())
                .build());

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        emailService.sendOrderConfirmation(order);
                    }
                }
        );

        return toOrderResponse(order);
    }

    @Transactional
    public OrderResponse selectCodPayment(String publicId) {
        Order order = orderRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NoSuchElementException(Message.NOT_FOUND));

        if (order.getCurrentStatus() == OrderStatus.CANCELLED
                || order.getCurrentStatus() == OrderStatus.COMPLETED) {
            throw new IllegalArgumentException("Không thể thanh toán đơn hàng này");
        }

        if (paymentTransactionRepository.existsByOrderAndStatus(order, PaymentStatus.PENDING)) {
            throw new IllegalArgumentException("Đã có giao dịch COD đang chờ xử lý");
        }

        order.setPaymentMethod(PaymentMethod.COD);

        paymentTransactionRepository.save(PaymentTransaction.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .method(PaymentMethod.COD)
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build());

        order.setCurrentStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);

        orderStatusHistoryRepository.save(OrderStatusHistory.builder()
                .order(order)
                .status(OrderStatus.PROCESSING)
                .note("COD - chờ thanh toán khi nhận hàng")
                .createdAt(LocalDateTime.now())
                .build());

        return toOrderResponse(order);
    }

    private static final String CROCKFORD = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
    private static final int ID_LENGTH = 6;

    private String generatePublicId() {
        String id;
        do {
            StringBuilder sb = new StringBuilder("TIMI-");
            for (int i = 0; i < ID_LENGTH; i++) {
                sb.append(CROCKFORD.charAt(ThreadLocalRandom.current().nextInt(CROCKFORD.length())));
            }
            id = sb.toString();
        } while (orderRepository.existsByPublicId(id));
        return id;
    }

    @Transactional
    public OrderResponse cancelOrder(String publicId) {
        Order order = orderRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NoSuchElementException(Message.NOT_FOUND));

        if (order.getCurrentStatus() == OrderStatus.COMPLETED
                || order.getCurrentStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Không thể hủy đơn hàng này");
        }

        order.setCurrentStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        orderStatusHistoryRepository.save(OrderStatusHistory.builder()
                .order(order)
                .status(OrderStatus.CANCELLED)
                .note("Khách hàng hủy")
                .createdAt(LocalDateTime.now())
                .build());

        return toOrderResponse(order);
    }

    private OrderResponse toOrderResponse(Order order) {
        return OrderResponse.builder()
                .publicId(order.getPublicId())
                .account(order.getAccount())
                .email(order.getEmail())
                .name(order.getName())
                .phone(order.getPhone())
                .address(order.getAddress())
                .note(order.getNote())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .expiresAt(order.getExpiresAt())
                .currentStatus(order.getCurrentStatus())
                .currentPaymentStatus(order.getCurrentPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .items(order.getItems().stream().map(this::toOrderItemResponse).toList())
                .statusHistory(order.getStatusHistory().stream().map(this::toOrderStatusHistoryResponse).toList())
                .build();
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        Sku sku = item.getSku();
        SkuResponse skuResponse = new SkuResponse(sku.getId(), sku.getSkuCode(), sku.getCategory(), sku.getSize(), sku.getPrice(), sku.getQuantity());
        CharacterDesign design = item.getCharacterDesign();
        CharacterDesignResponse designResponse = new CharacterDesignResponse(design.getId(), design.getName(), design.getImageUrl());
        return new OrderItemResponse(item.getId(), skuResponse, designResponse, item.getQuantity(), item.getPriceAtPurchase());
    }

    private OrderStatusHistoryResponse toOrderStatusHistoryResponse(OrderStatusHistory h) {
        return new OrderStatusHistoryResponse(h.getId(), h.getStatus(), h.getCreatedAt(), h.getNote());
    }
}
