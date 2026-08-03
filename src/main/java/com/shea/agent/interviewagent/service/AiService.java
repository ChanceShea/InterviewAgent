package com.shea.agent.interviewagent.service;

import com.shea.agent.interviewagent.dto.GraphRequest;
import com.shea.agent.interviewagent.dto.JobResumeMatchDTO;
import com.shea.agent.interviewagent.vo.GraphNodeResponse;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * @author : Shea.
 * @since : 2026/7/22 15:15
 */
public interface AiService {

//    Flux<ServerSentEvent<String>> chat(MultipartFile file,String input,String chatId,String phase);

    void chat(Sinks.Many<ServerSentEvent<GraphNodeResponse>> sink, GraphRequest request);

    @Deprecated
    Flux<ServerSentEvent<String>> evaluateAnswer(String chatId, String answer);

    JobResumeMatchDTO jdMatch(String chatId);

    void stopStreamProcessing(String chatId);
}
