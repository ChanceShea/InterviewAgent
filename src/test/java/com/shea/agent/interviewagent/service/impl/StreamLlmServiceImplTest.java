package com.shea.agent.interviewagent.service.impl;

import com.shea.agent.interviewagent.service.LlmService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StreamLlmServiceImplTest {

    @Resource
    private LlmService streamLlmService;

    @Test
    void call() {
        Flux<ChatResponse> call = streamLlmService.call(
                "你是一个Java专家，请用简洁的语言回答",  // system
                "什么是Spring Boot的自动配置？"
        );
        call.doOnNext(resp ->
                System.out.println(resp.getResult().getOutput().getText())
        ).blockLast();
    }
}