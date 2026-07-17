package com.example.timi_api.infrastructure.security.oauth2;

import com.example.timi_api.domain.entity.Account;
import com.example.timi_api.domain.entity.RefreshToken;
import com.example.timi_api.infrastructure.repository.AccountRepository;
import com.example.timi_api.infrastructure.repository.RefreshTokenRepository;
import com.example.timi_api.infrastructure.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final AccountRepository accountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final String clientUrl;

    public OAuth2SuccessHandler(
            JwtTokenProvider jwtTokenProvider,
            AccountRepository accountRepository,
            RefreshTokenRepository refreshTokenRepository,
            @Value("${cors.allowed-origins}") String allowedOrigins) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.accountRepository = accountRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.clientUrl = allowedOrigins.split(",")[0];
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found after OAuth2 login"));

        String accessToken = jwtTokenProvider.generateAccessToken(account.getId(), account.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(account.getId());

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(refreshToken)
                .accountId(account.getId())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true).secure(true).sameSite("None")
                .path("/").maxAge(Duration.ofDays(7)).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        getRedirectStrategy().sendRedirect(request, response,
                clientUrl + "/oauth2/callback?accessToken=" + accessToken);
    }
}
