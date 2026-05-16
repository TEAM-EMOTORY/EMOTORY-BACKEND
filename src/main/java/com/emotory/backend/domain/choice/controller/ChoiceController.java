package com.emotory.backend.domain.choice.controller;

import com.emotory.backend.domain.choice.controller.docs.ChoiceControllerDocs;
import com.emotory.backend.domain.choice.dto.request.ChoiceSelectRequest;
import com.emotory.backend.domain.choice.dto.response.ChoiceSelectResponse;
import com.emotory.backend.domain.choice.service.ChoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/play-sessions")
public class ChoiceController implements ChoiceControllerDocs {

    private final ChoiceService choiceService;

    @PostMapping("/{playSessionId}/choices")
    public ChoiceSelectResponse selectChoice(
            @PathVariable
            Long playSessionId,
            @RequestBody
            ChoiceSelectRequest request
    ) {

        return choiceService.selectChoice(
                playSessionId,
                request
        );
    }
}
