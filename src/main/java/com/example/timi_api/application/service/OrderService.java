package com.example.timi_api.application.service;

import com.example.timi_api.application.dto.request.CreateOrder;
import com.example.timi_api.application.dto.request.CreateOrderItem;
import com.example.timi_api.domain.constant.OrderStatus;
import com.example.timi_api.domain.constant.PaymentMethod;
import com.example.timi_api.domain.constant.PaymentStatus;
import com.example.timi_api.domain.entity.*;
import com.example.timi_api.domain.event.PaymentCompletedEvent;
import com.example.timi_api.infrastructure.email.EmailService;
import com.example.timi_api.infrastructure.message.Message;
import com.example.timi_api.infrastructure.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final ApplicationEventPublisher eventPublisher;

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

        OrderStatus initialStatus = request.getPaymentMethod() == PaymentMethod.COD
                ? OrderStatus.READY_TO_SHIP
                : OrderStatus.UNPAID;

        Order order = orderRepository.save(Order.builder()
                .publicId(generatePublicId())
                .account(account)
                .email(request.getEmail())
                .name(request.getName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .note(request.getNote())
                .currentStatus(initialStatus)
                .paymentMethod(request.getPaymentMethod())
                .build());

        BigDecimal total = BigDecimal.ZERO;
        for (CreateOrderItem item : request.getItems()) {
            Sku sku = skuRepository.getReferenceById(item.getSkuId());
            CharacterDesign design = characterDesignRepository.getReferenceById(item.getCharacterDesignId());
            BigDecimal price = sku.getPrice();
            total = total.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));

            orderItemRepository.save(OrderItem.builder()
                    .order(order)
                    .sku(sku)
                    .characterDesign(design)
                    .quantity(item.getQuantity())
                    .priceAtPurchase(price)
                    .build());
        }

        orderStatusHistoryRepository.save(OrderStatusHistory.builder()
                .order(order)
                .status(initialStatus)
                .createdAt(LocalDateTime.now())
                .build());

        if (request.getPaymentMethod() == PaymentMethod.COD) {
            BigDecimal codTotal = total;
            paymentTransactionRepository.save(PaymentTransaction.builder()
                    .order(order)
                    .amount(codTotal)
                    .method(PaymentMethod.COD)
                    .status(PaymentStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        emailService.sendOrderConfirmation(order);
                    }
                }
        );

        return order;
    }

    @Transactional
    public Order markCodAsPaid(String publicId) {
        Order order = orderRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NoSuchElementException(Message.NOT_FOUND));

        if (order.getPaymentMethod() != PaymentMethod.COD) {
            throw new IllegalArgumentException("Chỉ hỗ trợ thanh toán khi nhận hàng");
        }

        if (paymentTransactionRepository.existsByOrderAndStatus(order, PaymentStatus.PAID)) {
            throw new IllegalArgumentException("Đơn hàng đã được thanh toán");
        }

        paymentTransactionRepository.save(PaymentTransaction.builder()
                .order(order)
                .amount(calculateTotal(order))
                .method(PaymentMethod.COD)
                .status(PaymentStatus.PAID)
                .createdAt(LocalDateTime.now())
                .build());

        eventPublisher.publishEvent(new PaymentCompletedEvent(this, order));
        return order;
    }

    private String generatePublicId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return timestamp + random;
    }

    @Transactional
    public Order cancelOrder(String publicId) {
        Order order = orderRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NoSuchElementException(Message.NOT_FOUND));

        if (order.getCurrentStatus() == OrderStatus.COMPLETED
                || order.getCurrentStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Không thể hủy đơn hàng này");
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        List<CharacterDesign> designs = items.stream()
                .map(OrderItem::getCharacterDesign)
                .toList();

        orderItemRepository.deleteAll(items);
        characterDesignRepository.deleteAll(designs);

        order.setCurrentStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        orderStatusHistoryRepository.save(OrderStatusHistory.builder()
                .order(order)
                .status(OrderStatus.CANCELLED)
                .note("Khách hàng hủy")
                .createdAt(LocalDateTime.now())
                .build());

        return order;
    }

    private BigDecimal calculateTotal(Order order) {
        return order.getItems().stream()
                .map(item -> item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
