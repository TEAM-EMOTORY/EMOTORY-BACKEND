package com.emotory.backend.global.exception.choice;

import com.emotory.backend.global.exception.CustomException;
import com.emotory.backend.global.exception.ErrorCode;

public class ChoiceNotFoundException extends CustomException {
    public ChoiceNotFoundException() { super(ErrorCode.CHOICE_NOT_FOUND); }
}