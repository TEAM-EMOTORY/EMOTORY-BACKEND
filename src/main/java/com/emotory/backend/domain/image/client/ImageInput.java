package com.emotory.backend.domain.image.client;

import com.emotory.backend.global.exception.CustomException;
import com.emotory.backend.global.exception.ErrorCode;

import java.nio.charset.StandardCharsets;

/** 확장자가 아닌 실제 파일 헤더로 OpenAI에 전달할 이미지 형식을 정한다. */
public record ImageInput(byte[] bytes, String contentType, String extension) {
    public static ImageInput from(byte[] bytes) {
        if (bytes == null || bytes.length < 12 || bytes.length > 10 * 1024 * 1024) throw invalid();
        if (bytes[0] == (byte) 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G'
                && bytes[4] == 13 && bytes[5] == 10 && bytes[6] == 26 && bytes[7] == 10)
            return new ImageInput(bytes, "image/png", "png");
        if (bytes[0] == (byte) 0xff && bytes[1] == (byte) 0xd8 && bytes[2] == (byte) 0xff)
            return new ImageInput(bytes, "image/jpeg", "jpg");
        if (new String(bytes, 0, 4, StandardCharsets.US_ASCII).equals("RIFF")
                && new String(bytes, 8, 4, StandardCharsets.US_ASCII).equals("WEBP"))
            return new ImageInput(bytes, "image/webp", "webp");
        throw invalid();
    }
    private static CustomException invalid() { return new CustomException(ErrorCode.INVALID_FACE_IMAGE); }
}
