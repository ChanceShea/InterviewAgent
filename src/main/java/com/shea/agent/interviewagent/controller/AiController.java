package com.shea.agent.interviewagent.controller;

import com.shea.agent.interviewagent.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

/**
 * @author : Shea.
 * @since : 2026/7/20 17:44
 */
@RestController
@RequestMapping("/ai")
@Slf4j
@RequiredArgsConstructor
public class AiController {


    private final AiService aiService;


    @PostMapping(value = "/chat",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(
            @RequestParam("file") MultipartFile file,
            @RequestParam("input") String input,
            @RequestParam("chatId") String chatId
    ) {
        return aiService.chat(file, input, chatId);
    }
}
