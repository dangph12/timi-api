package com.example.timi_api.infrastructure.repository;

import com.example.timi_api.domain.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("update RefreshToken r set r.revoked = true where r.token = :token")
    int revokeByToken(@Param("token") String token);

    @Modifying
    @Query("update RefreshToken r set r.revoked = true where r.accountId = :accountId")
    int revokeByAccountId(@Param("accountId") Long accountId);
}
