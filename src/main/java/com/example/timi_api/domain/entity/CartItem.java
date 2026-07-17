package com.example.timi_api.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"account_id", "sku_id", "character_design_id"}))
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Account account;

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
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
