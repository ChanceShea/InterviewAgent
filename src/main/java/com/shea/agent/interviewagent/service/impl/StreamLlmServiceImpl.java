package com.shea.agent.interviewagent.service.impl;

import com.shea.agent.interviewagent.agent.AiAgent;
import com.shea.agent.interviewagent.service.LlmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * @author : Shea.
 * @since : 2026/7/19 16:52
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StreamLlmServiceImpl implements LlmService {

    private final AiAgent aiAgent;

    @Override
    public Flux<ChatResponse> call(String system, String user) {
        return aiAgent.getChatClient()
                .prompt(new Prompt(system))
                .user(user)
                .stream()
                .chatResponse();
    }

    @Override
    public Flux<ChatResponse> call(String system, String user, Class<?> classType) {
        StructuredOutputValidationAdvisor advisor = StructuredOutputValidationAdvisor.builder()
                .outputType(classType)
                .maxRepeatAttempts(2)
                .build();
        return Mono
                .fromCallable(() -> aiAgent.getChatClient()
                        .prompt(new Prompt(system))
                        .user(user)
                        .advisors(advisor)
                        .call()
                        .chatResponse())
        .subscribeOn(Schedulers.boundedElastic())
        .flux();
    }

    @Override
    public Flux<ChatResponse> callUser(String user) {
        return aiAgent.getChatClient()
                .prompt(new Prompt(user))
                .stream()
                .chatResponse();
    }
}
