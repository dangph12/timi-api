package com.example.timi_api.application.service;

import com.example.timi_api.application.dto.request.CreateCharacterDesign;
import com.example.timi_api.application.dto.request.CreateCharacterPartSelection;
import com.example.timi_api.domain.entity.CharacterDesign;
import com.example.timi_api.domain.entity.CharacterPartSelection;
import com.example.timi_api.infrastructure.message.Message;
import com.example.timi_api.infrastructure.repository.CharacterDesignRepository;
import com.example.timi_api.infrastructure.repository.CharacterPartSelectionRepository;
import com.example.timi_api.infrastructure.repository.PartOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CharacterDesignService {

    private final CharacterDesignRepository characterDesignRepository;
    private final PartOptionRepository partOptionRepository;
    private final CharacterPartSelectionRepository characterPartSelectionRepository;

    @Transactional
    public CharacterDesign createCharacterDesign(CreateCharacterDesign request) {

        for (CreateCharacterPartSelection selection : request.getPartSelections()) {
            partOptionRepository.findById(selection.getPartOptionId())
                    .orElseThrow(() -> new NoSuchElementException(Message.PART_OPTION_NOT_FOUND + selection.getPartOptionId()));
        }

        CharacterDesign characterDesign = characterDesignRepository.save(CharacterDesign.builder()
                .name(request.getName())
                .imageUrl(request.getImageUrl())
                .build());

        for (CreateCharacterPartSelection selection : request.getPartSelections()) {
            characterPartSelectionRepository.save(CharacterPartSelection.builder()
                    .characterDesign(characterDesign)
                    .partOption(partOptionRepository.getReferenceById(selection.getPartOptionId()))
                    .build());
        }
        return characterDesign;
    }
}
