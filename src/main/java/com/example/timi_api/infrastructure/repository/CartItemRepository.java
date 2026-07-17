package com.example.timi_api.infrastructure.repository;

import com.example.timi_api.domain.entity.CartItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Page<CartItem> findByAccountId(Long accountId, Pageable pageable);

    List<CartItem> findByAccountIdAndIdIn(Long accountId, List<Long> ids);

    Optional<CartItem> findByAccountIdAndSkuIdAndCharacterDesignId(Long accountId, Long skuId, Long characterDesignId);

    void deleteByAccountId(Long accountId);
}
