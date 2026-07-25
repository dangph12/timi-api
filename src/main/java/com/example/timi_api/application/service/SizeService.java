package com.example.timi_api.application.service;

import com.example.timi_api.application.dto.response.SizeResponse;
import com.example.timi_api.infrastructure.repository.SizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SizeService {

    private final SizeRepository sizeRepository;

    public List<SizeResponse> getAllSizes() {
        return sizeRepository.findAll().stream()
                .map(s -> new SizeResponse(s.getId(), s.getName()))
                .toList();
    }
}