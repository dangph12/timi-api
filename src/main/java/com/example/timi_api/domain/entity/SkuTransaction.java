package com.example.timi_api.domain.entity;

import com.example.timi_api.domain.constant.SkuTransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sku_transaction")
public class SkuTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sku_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Sku sku;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Order order;

    @Column(nullable = false)
    private Integer oldQuantity;

    @Column(nullable = false)
    private Integer newQuantity;

    @Column(nullable = false)
    private Integer changeAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkuTransactionType transactionType;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
