package com.example.timi_api.infrastructure.repository;

import com.example.timi_api.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
