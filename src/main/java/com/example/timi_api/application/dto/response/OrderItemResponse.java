package com.example.timi_api.application.dto.response;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class OrderItemResponse {
    Long id;
    SkuResponse sku;
    CharacterDesignResponse characterDesign;
    Integer quantity;
    BigDecimal priceAtPurchase;
}
