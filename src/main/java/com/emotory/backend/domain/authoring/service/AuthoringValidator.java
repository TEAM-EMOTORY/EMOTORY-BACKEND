package com.emotory.backend.domain.authoring.service;

import com.emotory.backend.domain.authoring.dto.AuthoringDocument;
import com.emotory.backend.global.exception.authoring.AuthoringException;
import com.emotory.backend.global.exception.ErrorCode;
import com.emotory.backend.domain.authoring.dto.AuthoringDocument.Node;
import com.emotory.backend.domain.authoring.dto.AuthoringDtos.*;
import com.emotory.backend.domain.interactiveStory.entity.StoryNodeType;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class AuthoringValidator {
    public void checkDraftIdentity(AuthoringDocument document) {
        Set<String> nodes = new HashSet<>();
        Set<String> choices = new HashSet<>();
        List<Issue> issues = new ArrayList<>();
        for (Node node : document.nodes()) {
            if (!nodes.add(node.id())) add(issues, "DUPLICATE_NODE_ID", node.id(), null, "id", "장면 ID가 중복됩니다.");
            for (var choice : node.choices()) {
                if (!choices.add(choice.id())) add(issues, "DUPLICATE_CHOICE_ID", node.id(), choice.id(), "id", "선택지 ID가 중복됩니다.");
            }
        }
        if (!issues.isEmpty()) throw new AuthoringException(ErrorCode.AUTHORING_DUPLICATE_ID, issues);
    }

    public ValidationResponse validate(AuthoringDocument document, long revision) {
        checkDraftIdentity(document);
        List<Issue> errors = new ArrayList<>();
        required(errors, document.title(), null, null, "title");
        required(errors, document.description(), null, null, "description");
        if (document.emotion() == null) add(errors, "EMOTION_REQUIRED", null, null, "emotion", "대표 감정을 지정하세요.");
        if (document.maxDepth() == null) add(errors, "MAX_DEPTH_REQUIRED", null, null, "maxDepth", "최대 선택 횟수를 지정하세요.");
        if (document.nodes().isEmpty()) add(errors, "EMPTY_STORY", null, null, "nodes", "장면을 추가하세요.");
        if (document.maxNodes() != null && document.nodes().size() > document.maxNodes())
            add(errors, "NODE_LIMIT_EXCEEDED", null, null, "maxNodes", "설정한 장면 수를 초과했습니다.");

        Map<String, Node> nodes = new LinkedHashMap<>();
        document.nodes().forEach(n -> nodes.put(n.id(), n));
        Node root = nodes.get(document.rootNodeId());
        if (root == null) add(errors, "ROOT_NOT_FOUND", document.rootNodeId(), null, "rootNodeId", "존재하는 시작 장면을 지정하세요.");
        else if (root.type() == StoryNodeType.ENDING)
            add(errors, "ROOT_MUST_BE_STORY", root.id(), null, "type", "시작 장면은 일반 장면으로 지정하세요.");

        Map<String, List<String>> edges = new LinkedHashMap<>();
        Map<String, Integer> indegrees = new LinkedHashMap<>();
        nodes.keySet().forEach(id -> { edges.put(id, new ArrayList<>()); indegrees.put(id, 0); });
        for (Node node : nodes.values()) {
            if (node.type() == null) add(errors, "NODE_TYPE_REQUIRED", node.id(), null, "type", "일반 장면 또는 결말을 지정하세요.");
            if (node.type() == StoryNodeType.STORY) {
                required(errors, node.content(), node.id(), null, "content");
                if (node.choices().isEmpty()) add(errors, "DEAD_END", node.id(), null, "choices", "선택지를 추가하거나 결말로 지정하세요.");
                if (node.ending() != null) add(errors, "UNEXPECTED_ENDING", node.id(), null, "ending", "일반 장면의 결말 데이터를 제거하세요.");
            }
            if (node.type() == StoryNodeType.ENDING) {
                if (!node.choices().isEmpty()) add(errors, "ENDING_HAS_CHOICES", node.id(), null, "choices", "결말에는 선택지를 둘 수 없습니다.");
                validateEnding(node, errors);
            }
            for (var choice : node.choices()) {
                required(errors, choice.text(), node.id(), choice.id(), "text");
                if (blank(choice.nextNodeId()))
                    add(errors, "UNCONNECTED_CHOICE", node.id(), choice.id(), "nextNodeId", "다음 장면을 선택하세요.");
                else if (!nodes.containsKey(choice.nextNodeId()))
                    add(errors, "TARGET_NOT_FOUND", node.id(), choice.id(), "nextNodeId", "연결 대상 장면이 존재하지 않습니다.");
                else {
                    edges.get(node.id()).add(choice.nextNodeId());
                    indegrees.merge(choice.nextNodeId(), 1, Integer::sum);
                }
            }
        }

        Set<String> reached = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        if (root != null) { reached.add(root.id()); queue.add(root.id()); }
        while (!queue.isEmpty()) {
            for (String next : edges.get(queue.remove())) if (reached.add(next)) queue.add(next);
        }
        // Missing root already explains why no reachability can be evaluated.
        if (root != null) for (Node node : nodes.values()) {
            if (!reached.contains(node.id())) add(errors,
                    node.type() == StoryNodeType.ENDING ? "UNREACHABLE_ENDING" : "UNREACHABLE_NODE",
                    node.id(), null, "id", "시작 장면에서 도달할 수 없습니다.");
        }

        List<String> order = new ArrayList<>();
        indegrees.forEach((id, degree) -> { if (degree == 0) queue.add(id); });
        while (!queue.isEmpty()) {
            String id = queue.remove();
            order.add(id);
            for (String next : edges.get(id)) if (indegrees.merge(next, -1, Integer::sum) == 0) queue.add(next);
        }
        boolean cycle = order.size() != nodes.size();
        if (cycle) add(errors, "CYCLE", null, null, "nodes", "반복되는 연결이 있습니다. 앞 장면으로 돌아가는 연결을 확인하세요.");
        Map<String, Integer> depths = new LinkedHashMap<>();
        Integer actualMaxDepth = null;
        if (!cycle && root != null) {
            depths.put(root.id(), 0);
            for (String id : order) {
                if (!depths.containsKey(id)) continue;
                for (String next : edges.get(id)) depths.merge(next, depths.get(id) + 1, Math::max);
            }
            actualMaxDepth = depths.values().stream().mapToInt(Integer::intValue).max().orElse(0);
            if (document.maxDepth() != null && actualMaxDepth > document.maxDepth())
                add(errors, "DEPTH_LIMIT_EXCEEDED", null, null, "maxDepth", "최대 선택 횟수를 초과하는 경로가 있습니다.");
        }
        return new ValidationResponse(revision, errors.isEmpty(), List.copyOf(errors), actualMaxDepth, depths);
    }

    private void validateEnding(Node node, List<Issue> errors) {
        var ending = node.ending();
        if (ending == null) {
            add(errors, "ENDING_REQUIRED", node.id(), null, "ending", "결말 내용과 감정 조언을 입력하세요.");
            return;
        }
        required(errors, ending.summary(), node.id(), null, "ending.summary");
        required(errors, ending.emotionLabel(), node.id(), null, "ending.emotionLabel");
        required(errors, ending.contentSummary(), node.id(), null, "ending.contentSummary");
        if (ending.advices() == null || ending.advices().isEmpty())
            add(errors, "ADVICE_REQUIRED", node.id(), null, "ending.advices", "조언을 하나 이상 입력하세요.");
        else for (int i = 0; i < ending.advices().size(); i++) {
            var advice = ending.advices().get(i);
            required(errors, advice.title(), node.id(), null, "ending.advices[" + i + "].title");
            required(errors, advice.description(), node.id(), null, "ending.advices[" + i + "].description");
        }
    }

    private static boolean blank(String value) { return value == null || value.isBlank(); }

    private static void required(List<Issue> errors, String value, String node, String choice, String field) {
        if (blank(value)) add(errors, "REQUIRED_FIELD", node, choice, field, "필수 내용을 입력하세요: " + field);
    }

    private static void add(List<Issue> errors, String code, String node, String choice, String field, String message) {
        errors.add(new Issue(code, node, choice, field, message));
    }
}
