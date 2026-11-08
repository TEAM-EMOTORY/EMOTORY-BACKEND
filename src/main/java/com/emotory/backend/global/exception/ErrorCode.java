package com.emotory.backend.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400 error
    INVALID_INPUT(400, "잘못된 입력 값입니다."),
    NO_FEED_UPDATE_CONTENT_EXCEPTION(400, "수정할 내용이 없습니다."),
    UNSUPPORTED_IMAGE_TYPE(400, "지원하지 않는 이미지 타입입니다."),
    UNSUPPORTED_FILE_FORMAT(400, "지원하지 않는 파일 형식입니다."),
    UNSUPPORTED_EMOTION_PATTERN(400, "지원하지 않는 감정 패턴입니다."),

    AUTHORING_DUPLICATE_ID(400, "초안의 ID가 중복됩니다."),
    AUTHORING_VALIDATION_FAILED(400, "공개할 수 없는 초안입니다. 검증 오류를 확인하세요."),

    // 401 error
    UNAUTHORIZED(401, "인증이 필요합니다."),
    INVALID_TOKEN(401, "유효하지 않거나 만료된 토큰입니다."),

    // 403 error
    FORBIDDEN(403, "접근 권한이 없습니다."),

    // 404 error
    NOT_FOUND(404, "존재하지 않는 리소스입니다."),
    MEMBER_NOT_FOUND(404, "회원을 찾을 수 없습니다."),
    S3_FILE_NOT_FOUND(404, "S3 파일을 찾을 수 없습니다."),
    PLAY_SESSION_NOT_FOUND(404, "플레이 세션을 찾을 수 없습니다."),
    STORY_NOT_FOUND(404, "존재하지 않는 스토리입니다.."),
    STORY_NODE_NOT_FOUND(404, "스토리 노드를 찾을 수 없습니다."),
    PLAY_HISTORY_NOT_FOUND(404, "존재하지 않는 플레이 기록입니다."),
    STORY_RESULT_NOT_FOUND(404, "존재하지 않는 스토리 결과입니다."),
    ADVICE_NOT_FOUND(404, "존재하지 않는 조언입니다."),
    CHOICE_NOT_FOUND(404,"존재하지 않는 선택지입니다."),
    EMOTION_DIARY_NOT_FOUND(404, "감정일기를 찾을 수 없습니다."),
    DIARY_QUESTION_NOT_FOUND(404, "감정일기 질문을 찾을 수 없습니다."),
    WEATHER_RECORD_NOT_FOUND(404, "감정일기 날씨 정보를 찾을 수 없습니다."),

    AUTHORING_DRAFT_NOT_FOUND(404, "동화 초안을 찾을 수 없습니다."),

    // 409 error
    STORY_UNPUBLISHED(409, "공개 중단된 동화는 새 플레이를 시작할 수 없습니다."),
    STORY_VERSION_UNAVAILABLE(409, "최신 공개본으로만 새 플레이를 시작할 수 있습니다. 동화 목록을 다시 조회하세요."),
    AUTHORING_PUBLICATION_DELETED(409, "기존 공개 동화가 없습니다. 공개 데이터를 확인하세요."),
    STORY_DELETE_PROTECTED(409, "플레이 세션이 있거나 공개 기록이 있는 동화는 삭제할 수 없습니다."),
    AUTHORING_REVISION_CONFLICT(409, "초안이 변경되었습니다. 최신 초안을 다시 조회하세요."),

    // 500 error
    INTERNAL_SERVER_ERROR(500, "서버 오류가 발생했습니다."),
    KMA_SERVICE_KEY_NOT_CONFIGURED(500, "기상청 API 서비스 키가 설정되지 않았습니다."),
    S3_DOWNLOAD_FAILED(500, "S3 파일 다운로드에 실패했습니다."),

    // 502 error
    OPEN_AI_IMAGE_FAILED(502, "OpenAI 이미지 생성에 실패했습니다.");

    private final int status;
    private final String message;

}
