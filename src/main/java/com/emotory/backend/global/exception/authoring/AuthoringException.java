package com.emotory.backend.global.exception.authoring;

import com.emotory.backend.domain.authoring.dto.AuthoringDtos.Issue;
import com.emotory.backend.global.exception.CustomException;
import com.emotory.backend.global.exception.ErrorCode;
import lombok.Getter;
import java.util.List;

@Getter
public class AuthoringException extends CustomException {
    private final List<Issue> errors;

    public AuthoringException(ErrorCode errorCode) {
        this(errorCode, List.of());
    }

    public AuthoringException(ErrorCode errorCode, List<Issue> errors) {
        super(errorCode);
        this.errors = List.copyOf(errors);
    }
}
