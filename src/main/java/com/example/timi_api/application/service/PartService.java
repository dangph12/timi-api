package com.example.timi_api.application.service;

import com.example.timi_api.application.dto.response.PartOptionResponse;
import com.example.timi_api.application.dto.response.PartResponse;
import com.example.timi_api.domain.entity.PartOption;
import com.example.timi_api.infrastructure.message.Message;
import com.example.timi_api.infrastructure.repository.PartOptionRepository;
import com.example.timi_api.infrastructure.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PartService {

    private final PartRepository partRepository;
    private final PartOptionRepository partOptionRepository;

    public List<PartResponse> getAllParts() {
        return partRepository.findAll().stream()
                .map(part -> new PartResponse(
                        part.getId(),
                        part.getName(),
                        part.getLayerOrder(),
                        part.getAllowMultiSelect()))
                .toList();
    }

    public List<PartOptionResponse> getPartOptions(Long partId, Long styleId) {
        partRepository.findById(partId)
                .orElseThrow(() -> new NoSuchElementException(Message.PART_NOT_FOUND + partId));

        List<PartOption> options;
        if (styleId != null) {
            options = partOptionRepository.findByPartIdAndStyleId(partId, styleId);
        } else {
            options = partOptionRepository.findByPartId(partId);
        }

        return options.stream()
                .map(opt -> new PartOptionResponse(
                        opt.getId(),
                        opt.getName(),
                        opt.getPart().getId(),
                        opt.getStyle() != null ? opt.getStyle().getId() : null,
                        opt.getImageUrl(),
                        opt.getDeltaX(),
                        opt.getDeltaY(),
                        opt.getDeltaScale(),
                        opt.getRotation(),
                        opt.getMutexGroupKey()))
                .toList();
    }
}
