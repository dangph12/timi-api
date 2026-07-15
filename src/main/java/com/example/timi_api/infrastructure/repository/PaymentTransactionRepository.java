package com.example.timi_api.infrastructure.repository;

import com.example.timi_api.domain.constant.PaymentStatus;
import com.example.timi_api.domain.entity.Order;
import com.example.timi_api.domain.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    List<PaymentTransaction> findByOrderId(Long orderId);

    boolean existsByOrderAndStatus(Order order, PaymentStatus status);

    boolean existsByTransactionReference(String transactionReference);

    Optional<PaymentTransaction> findByIdempotencyKey(String idempotencyKey);
}
