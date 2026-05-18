package com.emotory.backend.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * CustomException 처리
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        ErrorCode code = e.getErrorCode();

        log.warn("CustomException 발생: {}", code.name());

        return buildErrorResponse(code);
    }

    /**
     * PathVariable, RequestParam 타입 변환 실패 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        ErrorCode code = ErrorCode.INVALID_INPUT;

        log.warn("MethodArgumentTypeMismatchException 발생: parameter={}, value={}", e.getName(), e.getValue());

        return buildErrorResponse(code);
    }

    /**
     * 모든 예외 처리 (fallback)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {

        log.error("Unhandled Exception 발생", e);

        return ResponseEntity
                .status(500)
                .body(new ErrorResponse(500, "서버 내부 오류"));
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(ErrorCode code) {
        return ResponseEntity
                .status(code.getStatus())
                .body(new ErrorResponse(code.getStatus(), code.getMessage()));
    }
}
