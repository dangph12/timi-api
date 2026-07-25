package com.example.timi_api.infrastructure.repository;

import com.example.timi_api.domain.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SizeRepository extends JpaRepository<Size, Long> {
}