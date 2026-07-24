package com.example.timi_api.domain.entity;

import com.example.timi_api.domain.constant.OrderStatus;
import com.example.timi_api.domain.constant.PaymentMethod;
import com.example.timi_api.domain.constant.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String publicId;

    @Column(unique = true)
    private String idempotencyKey;

    @Version
    private Long version;

    @ManyToOne
    @JoinColumn(name = "account_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Account account;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String address;

    private String note;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus currentStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus currentPaymentStatus;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @OneToMany(mappedBy = "order")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public LocalDateTime getExpiresAt() {
        return createdAt != null ? createdAt.plusMinutes(10) : null;
    }
}
