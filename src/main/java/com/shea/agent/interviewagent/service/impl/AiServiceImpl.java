package com.shea.agent.interviewagent.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.GraphResponse;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.shea.agent.interviewagent.dto.AnswerUserQueryDTO;
import com.shea.agent.interviewagent.registry.FluxRegistry;
import com.shea.agent.interviewagent.service.AiService;
import com.shea.agent.interviewagent.utils.FileStorageUtil;
import com.shea.agent.interviewagent.utils.JsonUtil;
import com.shea.agent.interviewagent.utils.StateUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.shea.agent.interviewagent.constant.Constant.*;

/**
 * @author : Shea.
 * @since : 2026/7/22 15:16
 */
@Service
@Slf4j
public class AiServiceImpl implements AiService {

    private final CompiledGraph graph;
    private final FluxRegistry registry;

    public AiServiceImpl(StateGraph interviewGraph, FluxRegistry registry) throws GraphStateException {
        this.graph = interviewGraph.compile();
        this.registry = registry;
    }

    @Override
    public Flux<ServerSentEvent<String>> chat(
            MultipartFile file,
            String input,
            String chatId
    ) {
        String filePath;
        try {
            RunnableConfig config = RunnableConfig.builder()
                    .threadId(chatId)
                    .build();
            Map<String,Object> stateMap = new HashMap<>();
            stateMap.put(CHAT_ID,chatId);
            if (file != null && !file.isEmpty()) {
                filePath = FileStorageUtil.saveTempFile(file);
                stateMap.put(INPUT_FILE, filePath);
            } else {
                stateMap.put(INPUT_KEY,input);
            }
            return graph.stream(stateMap,config)
                    .flatMap(content -> {
                        if (content instanceof StreamingOutput output) {
                            if (GENERATE_QUESTION_NODE.equals(output.node())) {
                                return processGenerateQuestionNode(output);
                            } else if (ANSWER_WITH_RAG_NODE.equals(output.node())) {
                                return processAnswerWithRagNode(output);
                            }
                        }
                        return Flux.empty();
                    });
        }catch (Exception e){
            log.error("解析简历失败：{}",e.getMessage(),e);
            return Flux.just(ServerSentEvent.<String>builder()
                    .data("AI输出失败，请稍后再试")
                    .build());
        }
    }

    private Flux<ServerSentEvent<String>> processAnswerWithRagNode(StreamingOutput output) {
        String fluxId = StateUtil.getStringValue(output.state(), FLUX_ID);
        Flux<GraphResponse<StreamingOutput>> flux = registry.get(fluxId);
        if (flux == null) {
            return Flux.empty();
        }
        Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();
        StringBuilder sb = new StringBuilder();
        final String[] lastExtracted = {""};
        flux.subscribe(resp -> {
            if (resp.getOutput() == null) {
                return;
            }
            try {
                StreamingOutput streamingOutput = resp.getOutput().get();
                String chunk = handleResultStr(streamingOutput);
                if (chunk == null) return;
                sb.append(chunk);
                String fullText = sb.toString();
                String currentAnswer = JsonUtil.extractField(fullText,"answer");
                if (currentAnswer != null && currentAnswer.length() > lastExtracted[0].length()) {
                    String delta = currentAnswer.substring(lastExtracted[0].length());
                    if (!delta.isEmpty()) {
                        sink.tryEmitNext(delta);
                        lastExtracted[0] = currentAnswer;
                    }
                }
            } catch (Exception e) {
                sink.tryEmitError(e);
            }
        },err -> {
            log.error("流式输出错误",err);
            sink.tryEmitError(err);
        },() -> {
            log.info("流式输出完成");
            sink.tryEmitComplete();
        });
        return sink.asFlux()
                .map(content ->
                        ServerSentEvent.<String>builder()
                                .data(content)
                                .build()
                ).doOnCancel(() -> log.info("客户端断开连接"));
    }

    private String extractAnswer(String res) {
        if (JSONUtil.isTypeJSON(res)) {
            AnswerUserQueryDTO dto = JSONUtil.toBean(res, AnswerUserQueryDTO.class);
            return dto.getAnswer();
        }
        return res;
    }

    @NotNull
    private static String handleResultStr(StreamingOutput streamingOutput) {
        String resultStr = streamingOutput.getOriginData().toString();
        Pattern pattern = Pattern.compile("textContent=(.*?), metadata=", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(resultStr);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return resultStr;
    }

    @NotNull
    private Flux<ServerSentEvent<String>> processGenerateQuestionNode(StreamingOutput output) {
        String fluxId = StateUtil.getStringValue(output.state(), FLUX_ID);
        Flux<GraphResponse<StreamingOutput>> flux = registry.get(fluxId);
        if (flux != null) {
            return flux.flatMap(resp -> {
                try {
                    if (resp.getOutput() == null) {
                        return Flux.empty();
                    }
                    StreamingOutput streamingOutput = resp.getOutput().get();
                    String res = handleResultStr(streamingOutput);
                    return Flux.just(ServerSentEvent.<String>builder()
                            .data(res)
                            .build());
                } catch (Exception e) {
                    log.error("处理流式响应失败", e);
                    return Flux.just(ServerSentEvent.<String>builder()
                            .data("流式响应处理失败，请稍后再试")
                            .build());
                }
            });
        }
        return Flux.empty();
    }
}
