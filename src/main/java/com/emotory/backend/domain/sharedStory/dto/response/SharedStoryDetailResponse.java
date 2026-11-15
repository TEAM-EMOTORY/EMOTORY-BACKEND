package com.emotory.backend.domain.sharedStory.dto.response;

import com.emotory.backend.domain.playSession.entity.PlaySession;
import com.emotory.backend.domain.story.entity.Emotion;
import com.emotory.backend.domain.storyNode.entity.StoryNode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "공개 스토리 상세 응답")
public record SharedStoryDetailResponse(

        @Schema(description = "플레이 세션 ID")
        Long playSessionId,

        @Schema(description = "사용자 이름")
        String name,

        @Schema(description = "선택한 감정")
        Emotion emotion,

        @Schema(description = "스토리 제목")
        String title,

        @Schema(description = "실제 진행 순서의 장면과 마지막 결말")
        List<Node> nodes
) {

    public static SharedStoryDetailResponse from(PlaySession playSession, List<Node> nodes) {
        return new SharedStoryDetailResponse(
                playSession.getId(),
                playSession.getMember().getName(),
                playSession.getStory().getEmotion(),
                playSession.getStory().getTitle(),
                nodes
        );
    }

    @Schema(name = "SharedStoryNodeResponse", description = "실제로 진행한 스토리 장면")
    public record Node(
            @Schema(description = "스토리 노드 ID")
            Long id,
            @Schema(description = "해당 장면의 생성 이미지 URL")
            String imgSrc,
            @Schema(description = "해당 노드의 스토리 내용")
            String story
    ) {

        public static Node from(StoryNode node, String imageUrl) {
            return new Node(
                    node.getId(),
                    imageUrl,
                    node.getContent()
            );
        }
    }
}
