package com.example.timi_api.infrastructure.repository;

import com.example.timi_api.domain.entity.Part;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartRepository extends JpaRepository<Part, Long> {
}
