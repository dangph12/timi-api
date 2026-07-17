package com.example.timi_api.infrastructure.repository.base;

import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import java.util.List;

public class BaseRepositoryImpl<T, ID> extends SimpleJpaRepository<T, ID> {

    private static final Sort FALLBACK_SORT = Sort.by("id").ascending();

    public BaseRepositoryImpl(JpaEntityInformation<T, ?> entityInfo, EntityManager em) {
        super(entityInfo, em);
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort().and(FALLBACK_SORT)
                : FALLBACK_SORT;
        return super.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort));
    }

    @Override
    public List<T> findAll(Sort sort) {
        return super.findAll(sort.isSorted() ? sort.and(FALLBACK_SORT) : FALLBACK_SORT);
    }
}
