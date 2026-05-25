package com.emotory.backend.domain.image.service;

import com.emotory.backend.domain.storyNode.entity.StoryNode;
import org.springframework.stereotype.Service;

@Service
public class PromptTemplateService {

    public String buildPrompt(StoryNode node) {

        String stylePrompt = """
                  children's storybook illustration,
                  warm pastel colors,
                  soft lighting,
                  cute illustration,
                  emotional atmosphere,
                  high quality,
                  no text,
                  no letters,
                  no words,
                  no captions,
                  no speech bubbles,
                  no signs,
                  no typography,
                  no written characters
                  """;

        return stylePrompt + """
                  emotion:
                  """ + node.getEmotion() + """

                  instruction:
                  Create only an illustration of the scene. Do not include any text, letters, captions, labels, speech bubbles, signs, logos, or written characters in the image.

                  story scene:
                  """ + node.getContent();
    }
}