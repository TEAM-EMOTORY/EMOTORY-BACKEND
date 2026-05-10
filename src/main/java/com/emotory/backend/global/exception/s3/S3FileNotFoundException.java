package com.emotory.backend.global.exception.s3;

import com.emotory.backend.global.exception.CustomException;
import com.emotory.backend.global.exception.ErrorCode;

public class S3FileNotFoundException extends CustomException {
    public S3FileNotFoundException() { super(ErrorCode.S3_FILE_NOT_FOUND); }
}