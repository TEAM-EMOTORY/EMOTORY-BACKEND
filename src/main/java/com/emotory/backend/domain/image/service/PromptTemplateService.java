package com.emotory.backend.domain.image.service;

import com.emotory.backend.domain.storyNode.entity.StoryNode;
import org.springframework.stereotype.Service;

@Service
public class PromptTemplateService {

    public String buildPrompt(StoryNode node) {

        String stylePrompt = """
                    Create a cute 2D children's storybook illustration.
                    Use warm pastel colors and soft lighting.
                    Keep the same facial identity, age, and apparent gender as the input photo.
                    Do not change the child's gender.
                    Preserve the child's recognizable facial features.
                    Show an emotional atmosphere that matches the story scene.
                    Do not include any text, letters, words, captions, labels, speech bubbles, signs, logos, typography, or written characters.
                  """;

        return stylePrompt + """
                  emotion:
                  """ + node.getEmotion() + """
                  
                  story scene:
                  """ + node.getContent();
    }
}