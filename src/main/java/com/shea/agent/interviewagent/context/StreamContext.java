package com.shea.agent.interviewagent.context;

import lombok.Data;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Sinks;

/**
 * 流式处理上下文，封装相关状态
 * @author : Shea.
 * @since : 2026/7/24 10:12
 */
@Data
public class StreamContext {

    private String chatId;

    private Sinks.Many<ServerSentEvent<String>> sink;

    private String finalAnswer;
}
