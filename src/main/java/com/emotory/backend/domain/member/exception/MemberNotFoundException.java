package com.emotory.backend.domain.member.exception;

import com.emotory.backend.global.exception.CustomException;
import com.emotory.backend.global.exception.ErrorCode;

public class MemberNotFoundException extends CustomException {

    public MemberNotFoundException() {
        super(ErrorCode.NOT_FOUND);
    }
}
