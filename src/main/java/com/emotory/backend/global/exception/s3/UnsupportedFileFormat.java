package com.emotory.backend.global.exception.s3;

import com.emotory.backend.global.exception.CustomException;
import com.emotory.backend.global.exception.ErrorCode;

public class UnsupportedFileFormat extends CustomException {
    public UnsupportedFileFormat() {
        super(ErrorCode.UNSUPPORTED_FILE_FORMAT);
    }
}
