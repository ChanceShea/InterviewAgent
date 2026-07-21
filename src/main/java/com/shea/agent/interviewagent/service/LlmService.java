package com.shea.agent.interviewagent.service;

import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * @author : Shea.
 * @since : 2026/7/19 16:51
 */
public interface LlmService {

    Flux<ChatResponse> call(String system, String user);

    Flux<ChatResponse> call(String system, String user, Class<?> classType);

    Flux<ChatResponse> callUser(String user);
}
