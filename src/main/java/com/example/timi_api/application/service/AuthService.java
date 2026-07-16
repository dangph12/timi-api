package com.example.timi_api.application.service;

import com.example.timi_api.application.dto.request.LoginRequest;
import com.example.timi_api.application.dto.request.RegisterRequest;
import com.example.timi_api.application.dto.response.AuthResponse;
import com.example.timi_api.domain.constant.Role;
import com.example.timi_api.domain.entity.Account;
import com.example.timi_api.domain.entity.RefreshToken;
import com.example.timi_api.infrastructure.message.Message;
import com.example.timi_api.infrastructure.repository.AccountRepository;
import com.example.timi_api.infrastructure.repository.RefreshTokenRepository;
import com.example.timi_api.infrastructure.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AuthService {

    public record AuthResult(AuthResponse response, String refreshToken) {}

    private final AccountRepository accountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResult register(RegisterRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(Message.EMAIL_EXISTS);
        }

        Account account = new Account();
        account.setEmail(request.getEmail());
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setFullName(request.getFullName());
        account.setPhone(request.getPhone());
        account.setRole(Role.USER);
        account = accountRepository.save(account);

        return generateAuthResult(account);
    }

    @Transactional
    public AuthResult login(LoginRequest request) {
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException(Message.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            throw new IllegalArgumentException(Message.INVALID_CREDENTIALS);
        }

        return generateAuthResult(account);
    }

    @Transactional
    public AuthResult refresh(String refreshTokenValue) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new IllegalArgumentException(Message.REFRESH_TOKEN_INVALID));

        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(Message.REFRESH_TOKEN_INVALID);
        }

        refreshTokenRepository.delete(stored);

        return generateAuthResult(stored.getAccountId());
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElse(null);
        if (stored != null) {
            refreshTokenRepository.delete(stored);
        }
    }

    public AuthResponse getMe(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NoSuchElementException(Message.ACCOUNT_NOT_FOUND));
        return toAuthResponse(account, null);
    }

    private AuthResult generateAuthResult(Account account) {
        String accessToken = jwtTokenProvider.generateAccessToken(account.getId(), account.getRole());
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken(account.getId());

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(refreshTokenValue)
                .accountId(account.getId())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        AuthResponse response = toAuthResponse(account, accessToken);
        return new AuthResult(response, refreshTokenValue);
    }

    private AuthResult generateAuthResult(Long accountId) {
        String accessToken = jwtTokenProvider.generateAccessToken(accountId, null);
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken(accountId);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(refreshTokenValue)
                .accountId(accountId)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        AuthResponse response = AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(900L)
                .accountId(accountId)
                .build();
        return new AuthResult(response, refreshTokenValue);
    }

    private AuthResponse toAuthResponse(Account account, String accessToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(900L)
                .accountId(account.getId())
                .email(account.getEmail())
                .fullName(account.getFullName())
                .role(account.getRole().name())
                .build();
    }
}
