package com.example.timi_api.application.dto.response;

import lombok.Value;

@Value
public class CartItemResponse {
    Long id;
    SkuResponse sku;
    CharacterDesignResponse characterDesign;
    Integer quantity;
}
