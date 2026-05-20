package com.emotory.backend.global.exception.image;

import com.emotory.backend.global.exception.CustomException;
import com.emotory.backend.global.exception.ErrorCode;

public class OpenAiImageGenerationException extends CustomException {
    public OpenAiImageGenerationException() { super(ErrorCode.OPEN_AI_IMAGE_FAILED); }

    public OpenAiImageGenerationException(
            String message
    ) {
        super(
                ErrorCode.OPEN_AI_IMAGE_FAILED,
                message
        );
    }
}