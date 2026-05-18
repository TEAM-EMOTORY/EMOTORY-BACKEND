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

    // 401 error
    UNAUTHORIZED(401, "인증이 필요합니다."),
    INVALID_TOKEN(401, "유효하지 않거나 만료된 토큰입니다."),

    // 403 error
    FORBIDDEN(403, "접근 권한이 없습니다."),

    // 404 error
    NOT_FOUND(404, "존재하지 않는 리소스입니다."),
    S3_FILE_NOT_FOUND(404, "S3 파일을 찾을 수 없습니다."),
    STORY_RESULT_NOT_FOUND(404, "존재하지 않는 스토리 결과입니다."),
    ADVICE_NOT_FOUND(404, "존재하지 않는 조언입니다."),

    // 500 error
    INTERNAL_SERVER_ERROR(500, "서버 오류가 발생했습니다."),
    S3_DOWNLOAD_FAILED(500, "S3 파일 다운로드에 실패했습니다.");

    private final int status;
    private final String message;

}
