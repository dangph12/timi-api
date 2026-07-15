package com.example.timi_api.infrastructure.repository;

import com.example.timi_api.domain.constant.PaymentStatus;
import com.example.timi_api.domain.entity.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByPublicId(String publicId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.publicId = :publicId")
    Optional<Order> findByPublicIdForUpdate(@Param("publicId") String publicId);

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    boolean existsByPublicId(String publicId);

    List<Order> findByCurrentPaymentStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime before);
}
