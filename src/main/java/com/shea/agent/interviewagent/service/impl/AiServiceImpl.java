package com.shea.agent.interviewagent.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.shea.agent.interviewagent.context.JobMatchContext;
import com.shea.agent.interviewagent.dto.AnswerUserQueryDTO;
import com.shea.agent.interviewagent.dto.JobResumeMatchDTO;
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
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
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
    private final CompiledGraph matchGraph;
    private final FluxRegistry registry;
    private final JobMatchContext context;

    public AiServiceImpl(
            StateGraph interviewGraph,
            StateGraph jobResumeMatchGraph,
            FluxRegistry registry,
            JobMatchContext context
    ) throws GraphStateException {
        MemorySaver memorySaver = MemorySaver.builder().build();
        CompileConfig compileConfig = CompileConfig.builder()
                .saverConfig(SaverConfig.builder()
                        .register(memorySaver)
                        .build()
                ).build();
        this.graph = interviewGraph.compile(compileConfig);
        this.matchGraph = jobResumeMatchGraph.compile(compileConfig);
        this.registry = registry;
        this.context = context;
    }

    @Override
    public Flux<ServerSentEvent<String>> chat(
            MultipartFile file,
            String input,
            String chatId,
            String phase
    ) {
        try {
            RunnableConfig config = RunnableConfig.builder()
                    .threadId(chatId)
                    .build();
            Map<String,Object> stateMap = new HashMap<>();
            stateMap.put(CHAT_ID,chatId);
            if (file != null && !file.isEmpty()) {
                String filePath = FileStorageUtil.saveTempFile(file);
                stateMap.put(CURRENT_PHASE,INTERVIEW_PHASE);
                stateMap.put(INPUT_FILE, filePath);
            } else if (INTERVIEW_PHASE.equals(phase)) {
                stateMap.put(CURRENT_PHASE,INTERVIEW_PHASE);
                stateMap.put(USER_REPLY_ANSWER,input);
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
                            } else if (SUMMARIZE_INTERVIEW_NODE.equals(output.node())) {
                                return processSummarizeInterviewNode(output);
                            }
                        }
                        return Flux.empty();
                    });
        }catch (Exception e){
            log.error("处理请求失败：{}",e.getMessage(),e);
            return Flux.just(ServerSentEvent.<String>builder()
                    .data("AI输出失败，请稍后再试")
                    .build());
        }
    }

    private Flux<ServerSentEvent<String>> processSummarizeInterviewNode(StreamingOutput output) {
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
                String chunk = handleStreamStr(streamingOutput);
                if (chunk == null) {
                    return;
                }
                sb.append(chunk);
                String fullText = sb.toString();
                String currentAnswer = JsonUtil.extractField(fullText,"summary");
                if (currentAnswer != null && currentAnswer.length() > lastExtracted[0].length()) {
                    String delta = currentAnswer.substring(lastExtracted[0].length());
                    if (!delta.isEmpty()) {
                        sink.tryEmitNext(delta);
                        lastExtracted[0] = currentAnswer;
                    }
                }
            }catch (Exception e){
                sink.tryEmitError(e);
            }
        },err -> {
            log.error("流式输出出错，{}",err.getMessage());
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

    @Override
    public Flux<ServerSentEvent<String>> evaluateAnswer(String chatId, String answer) {
        RunnableConfig config = RunnableConfig.builder()
                .threadId(chatId)
                .build();
        Map<String,Object> stateMap = new HashMap<>();
        stateMap.put(CHAT_ID,chatId);
        stateMap.put(USER_REPLY_ANSWER,answer);
        return graph.stream(stateMap, config)
                .flatMap(output -> {
                    if (output instanceof StreamingOutput streamingOutput) {
                        if (EVALUATE_USER_QUERY_NODE.equals(streamingOutput.node())) {
                            return processEvaluateNode(streamingOutput);
                        }
                    }
                    return Flux.empty();
                });
    }

    @Override
    public JobResumeMatchDTO jdMatch(String chatId) {
        RunnableConfig config = RunnableConfig.builder()
                .threadId(chatId)
                .build();
        Map<String,Object> params = new HashMap<>();
        String jd = context.get(JD_PREFIX + chatId);
        String resumePath = context.get(RESUME_PREFIX + chatId);
        params.put(JOB_DESCRIPTION, jd);
        params.put(INPUT_FILE, resumePath);
        params.put(CHAT_ID,chatId);
        AtomicReference<JobResumeMatchDTO> dto = new AtomicReference<>();
        matchGraph.stream(params, config)
                .doOnNext(nodeOutput -> nodeOutput.state()
                        .value(MATCH_RESULT)
                        .ifPresent(result -> {
                            log.info("匹配结果：{}", result);
                            dto.set(JSONUtil.toBean(result.toString(), JobResumeMatchDTO.class));
                        }))
                .blockLast();
        return dto.get();
    }


    private Flux<ServerSentEvent<String>> processEvaluateNode(StreamingOutput output) {
        return output.state()
                .value(EVALUATIONS)
                .filter(evaluations -> {
                    if (evaluations instanceof List<?> list && !list.isEmpty()) {
                        return true;
                    }
                    log.warn("评估结果为空");
                    return false;
                })
                .map(evaluations -> ServerSentEvent.<String>builder()
                        .data(JSONUtil.toJsonStr(evaluations))
                        .build())
                .map(Flux::just)
                .orElseGet(Flux::empty);
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
                String chunk = handleStreamStr(streamingOutput);
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

    @Deprecated
    private String extractAnswer(String res) {
        if (JSONUtil.isTypeJSON(res)) {
            AnswerUserQueryDTO dto = JSONUtil.toBean(res, AnswerUserQueryDTO.class);
            return dto.getAnswer();
        }
        return res;
    }

    @NotNull
    private static String handleStreamStr(StreamingOutput streamingOutput) {
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
        String chatId = StateUtil.getStringValue(output.state(), CHAT_ID);
        Flux<GraphResponse<StreamingOutput>> flux = registry.get(fluxId);
        if (flux != null) {
            return flux.publishOn(Schedulers.boundedElastic())
                    .flatMap(resp -> {
                try {
                    if (resp.getOutput() == null) {
                        resp.resultValue().ifPresent(value -> {
                            if (value instanceof Map<?,?> map) {
                                Map<String,Object> update = new HashMap<>();
                                map.forEach((k,v) -> update.put(String.valueOf(k),v));
                                try {
                                    graph.updateState(
                                            RunnableConfig.builder()
                                                    .threadId(chatId)
                                                    .build(),
                                            update
                                    );
                                }catch (Exception e) {
                                    log.error("写入checkpoint失败",e);
                                }
                            }
                        });
                        return Flux.empty();
                    }
                    StreamingOutput streamingOutput = resp.getOutput().get();
                    String res = handleStreamStr(streamingOutput);
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
