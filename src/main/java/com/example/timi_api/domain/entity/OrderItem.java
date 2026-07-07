package com.example.timi_api.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Order order;

    @ManyToOne
    @JoinColumn(name = "sku_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Sku sku;

    @ManyToOne
    @JoinColumn(name = "character_design_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CharacterDesign characterDesign;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal priceAtPurchase;
}
