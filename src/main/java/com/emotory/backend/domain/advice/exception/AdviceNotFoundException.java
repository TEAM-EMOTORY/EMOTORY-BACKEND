package com.emotory.backend.domain.advice.exception;

import com.emotory.backend.global.exception.CustomException;
import com.emotory.backend.global.exception.ErrorCode;

public class AdviceNotFoundException extends CustomException {

    public AdviceNotFoundException() {
        super(ErrorCode.ADVICE_NOT_FOUND);
    }
}
