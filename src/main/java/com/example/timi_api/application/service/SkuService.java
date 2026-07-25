package com.example.timi_api.application.service;

import com.example.timi_api.application.dto.response.SkuResponse;
import com.example.timi_api.domain.constant.SkuQuantityLogType;
import com.example.timi_api.domain.entity.Category;
import com.example.timi_api.domain.entity.Order;
import com.example.timi_api.domain.entity.Size;
import com.example.timi_api.domain.entity.Sku;
import com.example.timi_api.domain.entity.SkuQuantityLog;
import com.example.timi_api.infrastructure.repository.SkuRepository;
import com.example.timi_api.infrastructure.repository.SkuQuantityLogRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SkuService {
    private final SkuRepository skuRepository;
    private final SkuQuantityLogRepository skuQuantityLogRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public SkuResponse createSku(String skuCode, Long categoryId, Long sizeId, BigDecimal price, Integer quantity) {
        Sku sku = new Sku();
        sku.setSkuCode(skuCode);
        sku.setCategory(entityManager.getReference(Category.class, categoryId));
        sku.setSize(entityManager.getReference(Size.class, sizeId));
        sku.setPrice(price);
        sku.setQuantity(quantity);
        skuRepository.save(sku);
        return new SkuResponse(sku.getId(), sku.getSkuCode(), sku.getCategory(), sku.getSize(), sku.getPrice(), sku.getQuantity());
    }

    @Transactional
    public void updateSku(Long id, String skuCode, Long categoryId, Long sizeId, BigDecimal price) {
        Sku sku = skuRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy SKU: " + id));
        sku.setSkuCode(skuCode);
        sku.setCategory(entityManager.getReference(Category.class, categoryId));
        sku.setSize(entityManager.getReference(Size.class, sizeId));
        sku.setPrice(price);
    }

    @Transactional
    public void adjustQuantity(Long skuId, int quantity, SkuQuantityLogType type, Order order) {
        Sku sku = skuRepository.findById(skuId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy SKU: " + skuId));

        int delta = type.applySign(quantity);
        int oldQuantity = sku.getQuantity();
        int newQuantity = oldQuantity + delta;

        if (newQuantity < 0) {
            throw new IllegalArgumentException("Số lượng hàng trong kho không đủ");
        }

        sku.setQuantity(newQuantity);

        skuQuantityLogRepository.save(SkuQuantityLog.builder()
                .sku(sku)
                .order(order)
                .oldQuantity(oldQuantity)
                .newQuantity(newQuantity)
                .changeAmount(delta)
                .type(type)
                .build());

        skuRepository.save(sku);
    }

    @Transactional
    public void adjustQuantity(Long skuId, int quantity, SkuQuantityLogType type) {
        adjustQuantity(skuId, quantity, type, null);
    }

    @Transactional
    public void deleteSku(Long id) {
        Sku sku = skuRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy SKU: " + id));
        skuRepository.delete(sku);
    }

    public SkuResponse getSkuById(Long id) {
        Sku sku = skuRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy SKU: " + id));
        return new SkuResponse(sku.getId(), sku.getSkuCode(), sku.getCategory(), sku.getSize(), sku.getPrice(), sku.getQuantity());
    }

    public Page<SkuResponse> getAllSkus(Pageable pageable) {
        return skuRepository.findAll(pageable)
                .map(sku -> new SkuResponse(
                        sku.getId(),
                        sku.getSkuCode(),
                        sku.getCategory(),
                        sku.getSize(),
                        sku.getPrice(),
                        sku.getQuantity()
                ));
    }

}
