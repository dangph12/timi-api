package com.example.timi_api.application.dto.response;

import lombok.Value;

@Value
public class PartResponse {
    Long id;
    String name;
    Long layerOrder;
    Boolean allowMultiSelect;
}
