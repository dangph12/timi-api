package com.example.timi_api.infrastructure.repository;

import com.example.timi_api.domain.entity.SkuQuantityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkuQuantityLogRepository extends JpaRepository<SkuQuantityLog, Long> {

    Page<SkuQuantityLog> findBySku_Id(Long skuId, Pageable pageable);
}