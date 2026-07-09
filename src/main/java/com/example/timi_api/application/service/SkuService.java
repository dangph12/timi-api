package com.example.timi_api.application.service;

import com.example.timi_api.application.dto.response.SkuResponse;
import com.example.timi_api.infrastructure.repository.SkuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkuService {
    private final SkuRepository skuRepository;

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
