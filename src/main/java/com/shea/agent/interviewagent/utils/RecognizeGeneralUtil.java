package com.shea.agent.interviewagent.utils;

import cn.hutool.json.JSONUtil;
import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.ocr_api20210707.AsyncClient;
import com.aliyun.sdk.service.ocr_api20210707.models.RecognizeBasicRequest;
import com.aliyun.sdk.service.ocr_api20210707.models.RecognizeBasicResponse;
import darabonba.core.client.ClientOverrideConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

/**
 * @author : Shea.
 * @since : 2026/7/30 17:10
 */
@Component
public class RecognizeGeneralUtil {

    @Value("${oss.accessKeyId}")
    private String accessKeyId;
    @Value("${oss.accessKeySecret}")
    private String accessKeySecret;

    public String recognize(RecognizeStrategy strategy) throws Exception {
        Credential credential = new Credential.Builder()
                .accessKeyId(accessKeyId)
                .accessKeySecret(accessKeySecret)
                .build();
        StaticCredentialProvider provider = StaticCredentialProvider.create(credential);

        try (AsyncClient client = AsyncClient.builder()
                .region("cn-hangzhou")
                .credentialsProvider(provider)
                .overrideConfiguration(
                        ClientOverrideConfiguration.create()
                                .setEndpointOverride("ocr-api.cn-hangzhou.aliyuncs.com")
                ).build()) {

            RecognizeBasicRequest request = strategy.buildRequest();
            CompletableFuture<RecognizeBasicResponse> response = client.recognizeBasic(request);
            RecognizeBasicResponse resp = response.get();
            return JSONUtil.toJsonStr(resp);
        }
    }

    @FunctionalInterface
    public interface RecognizeStrategy {
        RecognizeBasicRequest buildRequest();
    }

    public String recognizeByUrl(String url) throws Exception {
        return recognize(() -> RecognizeBasicRequest.builder()
                .url(url)
                .build());
    }

    public String recognizeByStream(InputStream inputStream) throws Exception {
        return recognize(() -> RecognizeBasicRequest.builder()
                .body(inputStream)
                .build());
    }
}
