package com.emotory.backend.global.exception.choice;

import com.emotory.backend.global.exception.CustomException;
import com.emotory.backend.global.exception.ErrorCode;

public class InValidInputException extends CustomException {
    public InValidInputException() { super(ErrorCode.INVALID_INPUT); }
}