package com.example.timi_api.infrastructure.web.v1;

import com.example.timi_api.application.dto.request.AddCartItemRequest;
import com.example.timi_api.application.dto.request.CartCheckoutRequest;
import com.example.timi_api.application.dto.request.UpdateCartItemQuantityRequest;
import com.example.timi_api.application.dto.response.CartItemResponse;
import com.example.timi_api.application.dto.response.OrderResponse;
import com.example.timi_api.application.service.CartService;
import com.example.timi_api.infrastructure.common.ApiResponse;
import com.example.timi_api.infrastructure.message.Message;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartItemResponse>> addItem(
            @AuthenticationPrincipal Long accountId,
            @Valid @RequestBody AddCartItemRequest request) {
        if (accountId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failed(Message.UNAUTHORIZED));
        }
        CartItemResponse item = cartService.addItem(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Message.CART_ITEM_ADDED, item));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getCartCount(
            @AuthenticationPrincipal Long accountId) {
        if (accountId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failed(Message.UNAUTHORIZED));
        }
        long count = cartService.getCartCount(accountId);
        return ResponseEntity.ok(ApiResponse.success(Message.CART_COUNT_SUCCESS, count));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CartItemResponse>>> getCart(
            @AuthenticationPrincipal Long accountId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        if (accountId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failed(Message.UNAUTHORIZED));
        }
        Page<CartItemResponse> cart = cartService.getCart(accountId, pageable);
        return ResponseEntity.ok(ApiResponse.success(Message.CART_GET_SUCCESS, cart));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateQuantity(
            @AuthenticationPrincipal Long accountId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemQuantityRequest request) {
        if (accountId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failed(Message.UNAUTHORIZED));
        }
        CartItemResponse item = cartService.updateQuantity(accountId, itemId, request);
        return ResponseEntity.ok(ApiResponse.success(Message.CART_ITEM_UPDATED, item));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartItemResponse>> removeItem(
            @AuthenticationPrincipal Long accountId,
            @PathVariable Long itemId) {
        if (accountId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failed(Message.UNAUTHORIZED));
        }
        CartItemResponse item = cartService.removeItem(accountId, itemId);
        return ResponseEntity.ok(ApiResponse.success(Message.CART_ITEM_REMOVED, item));
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            @AuthenticationPrincipal Long accountId,
            @Valid @RequestBody CartCheckoutRequest request) {
        if (accountId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failed(Message.UNAUTHORIZED));
        }
        OrderResponse order = cartService.checkout(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Message.CHECKOUT_SUCCESS, order));
    }
}
