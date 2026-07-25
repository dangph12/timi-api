package com.example.timi_api.infrastructure.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(String status, String message, @Nullable T data) {
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("success", message, data);
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>("success", message, null);
    }

    public static <T> ApiResponse<T> failed(String message) {
        return new ApiResponse<>("failed", message, null);
    }
}
