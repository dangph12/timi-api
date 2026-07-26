package com.example.timi_api.infrastructure.repository;

import com.example.timi_api.domain.constant.OrderStatus;
import com.example.timi_api.domain.constant.PaymentStatus;
import com.example.timi_api.domain.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByPublicId(String publicId);

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    boolean existsByPublicId(String publicId);

    List<Order> findByCurrentPaymentStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime before);

    Page<Order> findByAccount_Id(Long accountId, Pageable pageable);

    Page<Order> findByAccount_IdAndCurrentStatus(Long accountId, OrderStatus currentStatus, Pageable pageable);
}
