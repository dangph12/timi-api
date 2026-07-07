package com.example.timi_api.infrastructure.repository;

import com.example.timi_api.domain.entity.PartOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartOptionRepository extends JpaRepository<PartOption, Long> {
    List<PartOption> findByPartIdAndStyleId(Long partId, Long styleId);

    List<PartOption> findByPartId(Long partId);
}
