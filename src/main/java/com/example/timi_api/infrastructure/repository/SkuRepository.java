package com.example.timi_api.infrastructure.repository;

import com.example.timi_api.domain.entity.Sku;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkuRepository extends JpaRepository<Sku, Long> {
}
