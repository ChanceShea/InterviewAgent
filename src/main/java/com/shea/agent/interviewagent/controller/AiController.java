package com.shea.agent.interviewagent.controller;

import cn.hutool.core.util.StrUtil;
import com.shea.agent.interviewagent.common.Result;
import com.shea.agent.interviewagent.dto.GraphRequest;
import com.shea.agent.interviewagent.dto.JobResumeMatchDTO;
import com.shea.agent.interviewagent.exception.BusinessException;
import com.shea.agent.interviewagent.exception.ErrorCode;
import com.shea.agent.interviewagent.service.AiService;
import com.shea.agent.interviewagent.vo.GraphNodeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import static com.shea.agent.interviewagent.constant.Constant.EVENT_COMPLETE;
import static com.shea.agent.interviewagent.constant.Constant.EVENT_ERROR;

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
    public Flux<ServerSentEvent<GraphNodeResponse>> chat(
            @RequestParam(value = "file",required = false) MultipartFile file,
            @RequestParam(value = "input",required = false) String input,
            @RequestParam("chatId") String chatId,
            @RequestParam("phase") String phase
    ) {
        GraphRequest request = new GraphRequest();
        request.setChatId(chatId);
        request.setPhase(phase);
        request.setFile(file);
        request.setQuery(input);
        Sinks.Many<ServerSentEvent<GraphNodeResponse>> sink = Sinks.many().multicast().onBackpressureBuffer();
        aiService.chat(sink,request);
        return sink.asFlux().filter(sse -> {
                    // 1. 如果 event 是 "complete" 或 "error"，直接放行（不管 text 是否为空）
                    if (EVENT_COMPLETE.equals(sse.event()) || EVENT_ERROR.equals(sse.event())) {
                        return true;
                    }
                    // Protocol events carry state transitions in eventType and do not require
                    // display text.
                    if (sse.data() != null && sse.data().getEvent() != null) {
                        return true;
                    }
                    // 判断字符串是否为空
                    return sse.data() != null && sse.data().getContent() != null && !sse.data().getContent().isEmpty();
                })
                .doOnSubscribe(subscription -> log.info("Client subscribed to stream, threadId: {}", request.getChatId()))
                .doOnCancel(() -> {
                    log.info("Client disconnected from stream, threadId: {}", request.getChatId());
                    if (request.getChatId() != null) {
                        aiService.stopStreamProcessing(request.getChatId());
                    }
                })
                .doOnError(e -> {
                    log.error("Error occurred during streaming, threadId: {}: ", request.getChatId(), e);
                    if (request.getChatId() != null) {
                        aiService.stopStreamProcessing(request.getChatId());
                    }
                })
                .doOnComplete(() -> log.info("Stream completed successfully, threadId: {}", request.getChatId()));
    }

    @GetMapping("/match")
    public Result<JobResumeMatchDTO> jdMatch(@RequestParam String chatId) {
        if (StrUtil.isBlank(chatId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return Result.success(aiService.jdMatch(chatId));
    }
}
