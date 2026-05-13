package com.emotory.backend.global.exception.s3;

import com.emotory.backend.global.exception.CustomException;
import com.emotory.backend.global.exception.ErrorCode;

public class UnsupportedImageType extends CustomException {
    public UnsupportedImageType() {
        super(ErrorCode.UNSUPPORTED_IMAGE_TYPE);
    }
}
