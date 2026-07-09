package com.example.timi_api.application.dto.response;

import com.example.timi_api.domain.entity.Category;
import com.example.timi_api.domain.entity.Size;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class SkuResponse {
    Long id;
    String skuCode;
    Category category;
    Size size;
    BigDecimal price;
    Integer quantity;
}
