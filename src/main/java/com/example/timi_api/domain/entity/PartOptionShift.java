package com.example.timi_api.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
public class PartOptionShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_option_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PartOption sourceOption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_option_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PartOption relatedOption;

    @Column(nullable = false, name = "delta_x", columnDefinition = "FLOAT8 DEFAULT 0")
    private Double deltaX;

    @Column(nullable = false, name = "delta_y", columnDefinition = "FLOAT8 DEFAULT 0")
    private Double deltaY;

    @Column(columnDefinition = "FLOAT8 DEFAULT 1")
    private Double deltaScale = 1.0;

    @Column(columnDefinition = "FLOAT8 DEFAULT 0")
    private Double rotation = 0.0;
}
