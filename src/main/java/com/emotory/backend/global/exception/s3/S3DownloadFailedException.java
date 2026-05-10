package com.emotory.backend.global.exception.s3;

import com.emotory.backend.global.exception.CustomException;
import com.emotory.backend.global.exception.ErrorCode;

public class S3DownloadFailedException extends CustomException {
    public S3DownloadFailedException() { super(ErrorCode.S3_DOWNLOAD_FAILED); }
}