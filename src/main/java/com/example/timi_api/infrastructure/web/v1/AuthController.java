package com.example.timi_api.infrastructure.web.v1;

import com.example.timi_api.application.dto.request.LoginRequest;
import com.example.timi_api.application.dto.request.RegisterRequest;
import com.example.timi_api.application.dto.response.AuthResponse;
import com.example.timi_api.application.service.AuthService;
import com.example.timi_api.application.service.AuthService.AuthResult;
import com.example.timi_api.infrastructure.common.ApiResponse;
import com.example.timi_api.infrastructure.message.Message;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResult result = authService.register(request);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", result.refreshToken())
                .httpOnly(true).secure(true).sameSite("Strict")
                .path("/auth").maxAge(Duration.ofDays(7)).build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success(Message.REGISTER_SUCCESS, result.response()));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResult result = authService.login(request);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", result.refreshToken())
                .httpOnly(true).secure(true).sameSite("Strict")
                .path("/auth").maxAge(Duration.ofDays(7)).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success(Message.LOGIN_SUCCESS, result.response()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@CookieValue("refreshToken") String refreshToken) {
        AuthResult result = authService.refresh(refreshToken);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", result.refreshToken())
                .httpOnly(true).secure(true).sameSite("Strict")
                .path("/auth").maxAge(Duration.ofDays(7)).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success(Message.TOKEN_REFRESHED, result.response()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@CookieValue("refreshToken") String refreshToken) {
        authService.logout(refreshToken);
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true).secure(true).sameSite("Strict")
                .path("/auth").maxAge(0).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(ApiResponse.success(Message.LOGOUT_SUCCESS, null));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AuthResponse>> me(@AuthenticationPrincipal Long accountId) {
        AuthResponse response = authService.getMe(accountId);
        return ResponseEntity.ok(ApiResponse.success(Message.AUTH_SUCCESS, response));
    }
}
