package com.emotory.backend.global.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

public class OpenApiConfigurer {

    public OpenAPI createOpenAPI() {
        return new OpenAPI()
                .info(createInfo());
    }

    private Info createInfo() {
        return new Info()
                .title("Emotory API 문서")
                .version("v1.0.0")
                .description("Emotory 서비스 API 문서입니다.");
    }
}
