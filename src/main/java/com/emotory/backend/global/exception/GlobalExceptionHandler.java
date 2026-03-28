package com.emotory.backend.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

        return ResponseEntity
                .status(code.getStatus())
                .body(new ErrorResponse(code.getStatus(), code.getMessage()));
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
}
