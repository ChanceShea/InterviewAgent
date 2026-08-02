package com.shea.agent.interviewagent.utils;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RecognizeGeneralUtilTest {

    @Resource
    private RecognizeGeneralUtil recognizeGeneralUtil;

    @Test
    void recognize() throws Exception {
        String url = "https://ai-agent-1423417255.cos.ap-guangzhou.myqcloud.com/ai-reports/Snipaste_2026-07-30_16-50-05.png";
        String recognize = recognizeGeneralUtil.recognizeByUrl(url);
        System.out.println(recognize);
    }
}