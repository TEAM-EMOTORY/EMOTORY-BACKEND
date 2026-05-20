package com.emotory.backend.domain.image.dto.response;

import java.util.List;

public record OpenAiImageResponse(
        List<ImageData> data
) {
}
