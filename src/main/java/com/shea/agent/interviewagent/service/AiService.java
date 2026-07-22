package com.shea.agent.interviewagent.service;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

/**
 * @author : Shea.
 * @since : 2026/7/22 15:15
 */
public interface AiService {

    Flux<ServerSentEvent<String>> chat(MultipartFile file,String input,String chatId);
}
