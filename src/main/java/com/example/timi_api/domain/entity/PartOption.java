package com.example.timi_api.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@SQLDelete(sql = "UPDATE part_option SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class PartOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Part part;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "style_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Style style;

    @Column(nullable = false)
    private String imageUrl;

    @Column(name = "delta_x", columnDefinition = "FLOAT8 DEFAULT 0")
    private Double deltaX = 0.0;

    @Column(name = "delta_y", columnDefinition = "FLOAT8 DEFAULT 0")
    private Double deltaY = 0.0;

    @Column(columnDefinition = "FLOAT8 DEFAULT 1")
    private Double deltaScale = 1.0;

    @Column(columnDefinition = "FLOAT8 DEFAULT 0")
    private Double rotation = 0.0;

    private String mutexGroupKey;

    private LocalDateTime deletedAt;
}
