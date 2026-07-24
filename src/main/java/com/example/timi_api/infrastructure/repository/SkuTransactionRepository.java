package com.example.timi_api.infrastructure.repository;

import com.example.timi_api.domain.entity.SkuTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkuTransactionRepository extends JpaRepository<SkuTransaction, Long> {
}
