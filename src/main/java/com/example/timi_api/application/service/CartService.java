package com.example.timi_api.application.service;

import com.example.timi_api.application.dto.request.AddCartItemRequest;
import com.example.timi_api.application.dto.request.CartCheckoutRequest;
import com.example.timi_api.application.dto.request.UpdateCartItemQuantityRequest;
import com.example.timi_api.application.dto.response.CartItemResponse;
import com.example.timi_api.application.dto.response.CharacterDesignResponse;
import com.example.timi_api.application.dto.response.OrderResponse;
import com.example.timi_api.application.dto.response.SkuResponse;
import com.example.timi_api.domain.entity.*;
import com.example.timi_api.infrastructure.message.Message;
import com.example.timi_api.infrastructure.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final SkuRepository skuRepository;
    private final CharacterDesignRepository characterDesignRepository;
    private final AccountRepository accountRepository;
    private final OrderService orderService;

    @Transactional
    public CartItemResponse addItem(Long accountId, AddCartItemRequest request) {
        Sku sku = skuRepository.findById(request.getSkuId())
                .orElseThrow(() -> new NoSuchElementException(Message.SKU_NOT_FOUND + request.getSkuId()));
        characterDesignRepository.findById(request.getCharacterDesignId())
                .orElseThrow(() -> new NoSuchElementException(Message.DESIGN_NOT_FOUND + request.getCharacterDesignId()));

        CartItem existing = cartItemRepository
                .findByAccountIdAndSkuIdAndCharacterDesignId(accountId, request.getSkuId(), request.getCharacterDesignId())
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
            cartItemRepository.save(existing);
            return toCartItemResponse(existing);
        }

        Account account = accountRepository.getReferenceById(accountId);

        CartItem item = cartItemRepository.save(CartItem.builder()
                .account(account)
                .sku(skuRepository.getReferenceById(request.getSkuId()))
                .characterDesign(characterDesignRepository.getReferenceById(request.getCharacterDesignId()))
                .quantity(request.getQuantity())
                .build());

        return toCartItemResponse(item);
    }

    public long getCartCount(Long accountId) {
        return cartItemRepository.countByAccountId(accountId);
    }

    public Page<CartItemResponse> getCart(Long accountId, Pageable pageable) {
        return cartItemRepository.findByAccountId(accountId, pageable)
                .map(this::toCartItemResponse);
    }

    @Transactional
    public CartItemResponse updateQuantity(Long accountId, Long itemId, UpdateCartItemQuantityRequest request) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException(Message.CART_ITEM_NOT_FOUND));

        if (!item.getAccount().getId().equals(accountId)) {
            throw new NoSuchElementException(Message.CART_ITEM_NOT_FOUND);
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        return toCartItemResponse(item);
    }

    @Transactional
    public CartItemResponse removeItem(Long accountId, Long itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException(Message.CART_ITEM_NOT_FOUND));

        if (!item.getAccount().getId().equals(accountId)) {
            throw new NoSuchElementException(Message.CART_ITEM_NOT_FOUND);
        }

        CartItemResponse response = toCartItemResponse(item);
        cartItemRepository.delete(item);
        return response;
    }

    @Transactional
    public OrderResponse checkout(Long accountId, CartCheckoutRequest request) {
        List<CartItem> items = cartItemRepository.findByAccountIdAndIdIn(accountId, request.getCartItemIds());

        if (items.isEmpty()) {
            throw new IllegalArgumentException(Message.CART_ITEMS_NOT_EMPTY);
        }

        if (items.size() != request.getCartItemIds().size()) {
            throw new NoSuchElementException(Message.CART_ITEM_NOT_FOUND);
        }

        OrderResponse order = orderService.createOrderFromCartItems(
                request.getEmail(), request.getName(), request.getPhone(),
                request.getAddress(), request.getNote(), items, accountId);

        cartItemRepository.deleteAll(items);

        return order;
    }

    private CartItemResponse toCartItemResponse(CartItem item) {
        Sku sku = item.getSku();
        SkuResponse skuResponse = new SkuResponse(sku.getId(), sku.getSkuCode(), sku.getCategory(), sku.getSize(), sku.getPrice(), sku.getQuantity());
        CharacterDesign design = item.getCharacterDesign();
        CharacterDesignResponse designResponse = new CharacterDesignResponse(design.getId(), design.getName(), design.getImageUrl());
        return new CartItemResponse(item.getId(), skuResponse, designResponse, item.getQuantity());
    }
}
