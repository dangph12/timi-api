package com.example.timi_api.infrastructure.repository;

import com.example.timi_api.domain.entity.CharacterDesign;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacterDesignRepository extends JpaRepository<CharacterDesign, Long> {
}
