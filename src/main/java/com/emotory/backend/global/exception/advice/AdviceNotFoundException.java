package com.emotory.backend.global.exception.advice;

import com.emotory.backend.global.exception.CustomException;
import com.emotory.backend.global.exception.ErrorCode;

public class AdviceNotFoundException extends CustomException {

    public AdviceNotFoundException() {
        super(ErrorCode.ADVICE_NOT_FOUND);
    }
}
