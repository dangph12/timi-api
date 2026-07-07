package com.example.timi_api.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class CharacterPartSelection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "character_design_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CharacterDesign characterDesign;

    @ManyToOne
    @JoinColumn(name = "part_option_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PartOption partOption;
}
