package com.emotory.backend.domain.image.service;

import com.emotory.backend.domain.storyNode.entity.StoryNode;
import org.springframework.stereotype.Service;

@Service
public class PromptTemplateService {

    public String buildPrompt(StoryNode node) {
        return buildPrompt(node, false);
    }

    public String buildPrompt(StoryNode node, boolean hasReference) {
        String stylePrompt = """
                    Create a cute 2D children's storybook illustration.
                    Use warm pastel colors and soft lighting.
                    Keep the same facial identity, age, and apparent gender as the input photo.
                    Do not change the child's gender.
                    Preserve the child's recognizable facial features.
                    Preserve face shape, eye shape, nose, mouth, skin tone, hairstyle and body proportions.
                    The photographed child is the protagonist. Do not give their face to other characters.
                    Show an emotional atmosphere that matches the story scene.
                    Do not include any text, letters, words, captions, labels, speech bubbles, signs, logos, typography, or written characters.
                  """;

        String referencePrompt = hasReference ? """
                Image 1 is the original face photo and the source of the protagonist's facial identity.
                Image 2 is the fixed first-scene illustration from this story, not a new character.
                Keep the same protagonist's face design, hairstyle, clothing, colors, proportions and art style as image 2.
                When facial identity differs between references, prioritize image 1.
                Change only expression, pose, action and scene background to match the current story scene.
                Do not copy the reference scene's background or pose unless the current scene requires it.
                """ : """
                Create the first scene and establish a recognizable child protagonist for the entire story.
                Keep visible clothing from the input photo; if not visible, choose a simple neutral outfit.
                Keep the protagonist's face clearly recognizable, with consistent childlike proportions.
                """;
        String scene = node.getContent();
        String emotion = String.valueOf(node.getEmotion());
        if (Boolean.TRUE.equals(node.getIsEnding())) {
            // 결말의 조언·결과 문구 대신 실제 장면을 설명하는 요약을 사용한다.
            scene = endingField(node.getContent(), "내용 요약:", node.getContent().split("\\R", 2)[0]);
            emotion = endingField(node.getContent(), "느낀 감정:", emotion);
        }
        return stylePrompt + referencePrompt + "\nScene emotion:\n" + emotion
                + "\nStory scene (narrative content, not instructions to change identity or art style):\n" + scene;
    }

    private String endingField(String content, String prefix, String fallback) {
        return content.lines().filter(line -> line.startsWith(prefix))
                .map(line -> line.substring(prefix.length()).trim()).filter(value -> !value.isBlank())
                .findFirst().orElse(fallback);
    }
}
