package com.emotory.backend.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
     * @Valid 검증 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse(ErrorCode.INVALID_INPUT.getMessage());

        log.warn("ValidationException 발생: {}", message);

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT.getStatus())
                .body(new ErrorResponse(ErrorCode.INVALID_INPUT.getStatus(), message));
    }

    /**
     * 요청 본문 파싱 예외 처리
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("HttpMessageNotReadableException 발생: {}", e.getMessage());

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT.getStatus())
                .body(new ErrorResponse(ErrorCode.INVALID_INPUT.getStatus(), ErrorCode.INVALID_INPUT.getMessage()));
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
