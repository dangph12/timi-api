package com.example.timi_api.application.dto.response;

import lombok.Value;

@Value
public class PartOptionResponse {
    Long id;
    String name;
    Long partId;
    Long styleId;
    String imageUrl;
    Double deltaX;
    Double deltaY;
    Double deltaScale;
    Double rotation;
    String mutexGroupKey;
}
