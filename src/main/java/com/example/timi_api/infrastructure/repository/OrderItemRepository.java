package com.example.timi_api.infrastructure.repository;

import com.example.timi_api.domain.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
