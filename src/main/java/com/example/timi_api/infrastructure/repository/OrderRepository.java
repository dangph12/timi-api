package com.example.timi_api.infrastructure.repository;

import com.example.timi_api.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
