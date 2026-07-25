package com.example.timi_api.infrastructure.repository;

import com.example.timi_api.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}