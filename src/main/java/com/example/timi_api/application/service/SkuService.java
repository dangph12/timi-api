package com.example.timi_api.application.service;

import com.example.timi_api.application.dto.response.SkuResponse;
import com.example.timi_api.domain.constant.SkuTransactionType;
import com.example.timi_api.domain.entity.Order;
import com.example.timi_api.domain.entity.Sku;
import com.example.timi_api.domain.entity.SkuTransaction;
import com.example.timi_api.infrastructure.repository.SkuRepository;
import com.example.timi_api.infrastructure.repository.SkuTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SkuService {
    private final SkuRepository skuRepository;
    private final SkuTransactionRepository skuTransactionRepository;

    @Transactional
    public void adjustQuantity(Long skuId, int changeAmount, SkuTransactionType type, Order order) {
        Sku sku = skuRepository.findById(skuId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy SKU: " + skuId));

        int oldQuantity = sku.getQuantity();
        int newQuantity = oldQuantity + changeAmount;

        if (newQuantity < 0) {
            throw new IllegalArgumentException("Số lượng hàng trong kho không đủ");
        }

        sku.setQuantity(newQuantity);

        skuTransactionRepository.save(SkuTransaction.builder()
                .sku(sku)
                .order(order)
                .oldQuantity(oldQuantity)
                .newQuantity(newQuantity)
                .changeAmount(changeAmount)
                .transactionType(type)
                .build());

        skuRepository.save(sku);
    }

    public List<SkuResponse> getAllSkus() {
        return skuRepository.findAll().stream()
                .map(sku -> new SkuResponse(
                        sku.getId(),
                        sku.getSkuCode(),
                        sku.getCategory(),
                        sku.getSize(),
                        sku.getPrice(),
                        sku.getQuantity()
                )).toList();
    }

}
