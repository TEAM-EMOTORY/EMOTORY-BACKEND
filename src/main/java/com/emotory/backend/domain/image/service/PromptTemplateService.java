package com.emotory.backend.domain.image.service;

import com.emotory.backend.domain.storyNode.entity.StoryNode;
import org.springframework.stereotype.Service;

@Service
public class PromptTemplateService {

    public String buildPrompt(StoryNode node) {

        String stylePrompt = """
                children's storybook style,
                disney pixar style,
                warm pastel colors,
                soft lighting,
                cute illustration,
                emotional atmosphere,
                high quality
                """;

        return stylePrompt + """
                emotion:
                """ + node.getEmotion() + """

                story scene:
                """ + node.getContent();
    }
}
