package com.emotory.backend.domain.authoring.service;

import com.emotory.backend.domain.authoring.dto.AuthoringDocument;
import com.emotory.backend.domain.authoring.dto.AuthoringDtos.Issue;
import com.emotory.backend.domain.choice.entity.Choice;
import com.emotory.backend.domain.choice.repository.ChoiceRepository;
import com.emotory.backend.domain.interactiveStory.entity.StoryNodeType;
import com.emotory.backend.domain.story.entity.Story;
import com.emotory.backend.domain.story.repository.StoryRepository;
import com.emotory.backend.domain.storyNode.entity.StoryNode;
import com.emotory.backend.domain.storyNode.repository.StoryNodeRepository;
import com.emotory.backend.global.exception.ErrorCode;
import com.emotory.backend.global.exception.authoring.AuthoringException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.Collectors;

/** 기존 스키마를 변경하지 않고 검증된 초안을 플레이 데이터로 변환한다. */
@Component
@RequiredArgsConstructor
public class AuthoringPublicationMapper {
    private final StoryRepository stories;
    private final StoryNodeRepository nodes;
    private final ChoiceRepository choices;

    public Story createStory(AuthoringDocument document) {
        checkEndingFormat(document);
        Story story = stories.save(Story.create(document.title(), document.description(), document.emotion()));
        Map<String, StoryNode> persisted = new LinkedHashMap<>();
        int order = 1;
        for (var dto : orderedNodes(document)) {
            boolean ending = dto.type() == StoryNodeType.ENDING;
            String content = ending ? endingContent(dto.ending()) : dto.content();
            // 초안의 길이 제한은 문자 수 기준이지만 MySQL TEXT의 저장 한도는 바이트 기준이다.
            if (content.getBytes(StandardCharsets.UTF_8).length > 65535) {
                throw new AuthoringException(ErrorCode.AUTHORING_VALIDATION_FAILED, List.of(
                        new Issue("PLAY_CONTENT_TOO_LARGE", dto.id(), null, "content", "플레이 장면의 UTF-8 크기가 TEXT 저장 한도를 초과합니다.")));
            }
            var node = nodes.save(StoryNode.create(story, content, order++, ending,
                    dto.emotion() == null ? document.emotion() : dto.emotion()));
            persisted.put(dto.id(), node);
        }
        for (var dto : document.nodes()) {
            for (var choice : dto.choices()) {
                choices.save(Choice.create(persisted.get(dto.id()), choice.text(), persisted.get(choice.nextNodeId())));
            }
        }
        return story;
    }

    // 기존 플레이 시작 로직이 지정한 시작 장면을 선택하도록 첫 순서에 배치한다.
    private List<AuthoringDocument.Node> orderedNodes(AuthoringDocument document) {
        return Stream.concat(
                document.nodes().stream().filter(n -> n.id().equals(document.rootNodeId())),
                document.nodes().stream().filter(n -> !n.id().equals(document.rootNodeId()))).toList();
    }

    private String endingContent(AuthoringDocument.Ending ending) {
        String advice = ending.advices().stream()
                .map(a -> "조언: " + a.title() + " | " + a.description())
                .collect(Collectors.joining("\n"));
        return String.join("\n", ending.summary(), "느낀 감정: " + ending.emotionLabel(),
                "내용 요약: " + ending.contentSummary(), advice);
    }

    private void checkEndingFormat(AuthoringDocument document) {
        List<Issue> errors = new ArrayList<>();
        for (var node : document.nodes()) {
            if (node.type() != StoryNodeType.ENDING) continue;
            var ending = node.ending();
            checkSingleLine(errors, node.id(), "ending.summary", ending.summary());
            checkSingleLine(errors, node.id(), "ending.emotionLabel", ending.emotionLabel());
            checkSingleLine(errors, node.id(), "ending.contentSummary", ending.contentSummary());
            // 기존 해석기가 요약을 감정 또는 조언으로 중복 해석하는 입력도 차단한다.
            if (ending.summary().startsWith("조언:") || ending.summary().startsWith("느낀 감정:")) {
                errors.add(new Issue("LEGACY_ENDING_RESERVED_PREFIX", node.id(), null, "ending.summary",
                        "결말 요약은 '조언:' 또는 '느낀 감정:'으로 시작할 수 없습니다."));
            }
            for (int i = 0; i < ending.advices().size(); i++) {
                var advice = ending.advices().get(i);
                String field = "ending.advices[" + i + "]";
                checkSingleLine(errors, node.id(), field + ".title", advice.title());
                checkSingleLine(errors, node.id(), field + ".description", advice.description());
                if (advice.title().contains("|")) {
                    errors.add(new Issue("LEGACY_ADVICE_SEPARATOR", node.id(), null, field + ".title",
                            "조언 제목에는 제목과 설명의 구분자인 '|'를 사용할 수 없습니다."));
                }
            }
        }
        if (!errors.isEmpty()) throw new AuthoringException(ErrorCode.AUTHORING_VALIDATION_FAILED, errors);
    }

    private void checkSingleLine(List<Issue> errors, String nodeId, String field, String value) {
        if (value.contains("\n") || value.contains("\r")) {
            errors.add(new Issue("LEGACY_ENDING_LINE_BREAK", nodeId, null, field,
                    "기존 결말 형식으로 공개하려면 해당 항목을 줄바꿈 없이 입력하세요."));
        }
    }
}
