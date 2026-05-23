package com.emotory.backend.global.exception.member;

import com.emotory.backend.global.exception.CustomException;
import com.emotory.backend.global.exception.ErrorCode;

public class MemberNotFoundException extends CustomException {

    public MemberNotFoundException() {
        super(ErrorCode.MEMBER_NOT_FOUND);
    }
}
