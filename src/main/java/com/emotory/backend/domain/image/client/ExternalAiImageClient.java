package com.emotory.backend.domain.image.client;

import com.emotory.backend.domain.image.dto.response.OpenAiImageResponse;
import com.emotory.backend.global.exception.image.OpenAiImageGenerationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;

@Component
@RequiredArgsConstructor
public class ExternalAiImageClient {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.base-url}")
    private String baseUrl;

    private final WebClient.Builder webClientBuilder;

    public byte[] generateImage(
            byte[] faceImage,
            String prompt
    ) {
        return generateImage(faceImage, null, prompt);
    }

    public byte[] generateImage(byte[] faceImage, byte[] referenceImage, String prompt) {

        WebClient webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();

        MultipartBodyBuilder builder =
                new MultipartBodyBuilder();

        // 입력 순서를 고정해 프롬프트의 이미지 1·2와 일치시킨다.
        addImage(builder, faceImage, "face");
        if (referenceImage != null) addImage(builder, referenceImage, "first-scene");

        // 프롬프트
        builder.part("prompt", prompt);

        // 모델
        builder.part("model", "gpt-image-2.5-flare");

        // 비용 절약 옵션
        builder.part("quality", "low");

        // 이미지 크기 축소
        builder.part("size", "1024x1024");

        OpenAiImageResponse response =
                webClient.post()
                        .uri("/v1/images/edits")
                        .header("Authorization", "Bearer " + apiKey)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .bodyValue(builder.build())
                        .retrieve()
                        .onStatus(
                                status -> status.isError(),
                                clientResponse ->
                                        clientResponse
                                                .bodyToMono(String.class)
                                                .map(errorBody -> {
                                                    System.out.println(
                                                            errorBody
                                                    );

                                                    return new OpenAiImageGenerationException(
                                                            errorBody
                                                    );
                                                })
                        )
                        .bodyToMono(OpenAiImageResponse.class)
                        .block();

        if (response == null
                || response.data() == null
                || response.data().isEmpty()) {
            throw new OpenAiImageGenerationException();
        }

        String base64Image =
                response.data().get(0).b64_json();

        return Base64.getDecoder().decode(base64Image);
    }

    private void addImage(MultipartBodyBuilder builder, byte[] bytes, String name) {
        ImageInput input = ImageInput.from(bytes);
        builder.part("image[]", new ByteArrayResource(bytes) {
            @Override
            public String getFilename() { return name + "." + input.extension(); }
        }).contentType(MediaType.parseMediaType(input.contentType()));
    }
}
