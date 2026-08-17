package com.shea.agent.interviewagent.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.checkpoint.BaseCheckpointSaver;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.shea.agent.interviewagent.context.JobMatchContext;
import com.shea.agent.interviewagent.context.StreamContext;
import com.shea.agent.interviewagent.dto.AnswerUserQueryDTO;
import com.shea.agent.interviewagent.dto.GraphRequest;
import com.shea.agent.interviewagent.dto.JobResumeMatchDTO;
import com.shea.agent.interviewagent.exception.BusinessException;
import com.shea.agent.interviewagent.exception.ErrorCode;
import com.shea.agent.interviewagent.registry.FluxRegistry;
import com.shea.agent.interviewagent.registry.StreamContextRegistry;
import com.shea.agent.interviewagent.service.AiService;
import com.shea.agent.interviewagent.utils.FileStorageUtil;
import com.shea.agent.interviewagent.utils.JsonUtil;
import com.shea.agent.interviewagent.utils.StateUtil;
import com.shea.agent.interviewagent.vo.GraphNodeResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
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
    private final JobMatchContext jobContext;
    private final StreamContextRegistry contextRegistry;
    private final ExecutorService executor;
    private final CompileConfig compileConfig;
    private final BaseCheckpointSaver checkpointSaver;

    public AiServiceImpl(
            StateGraph interviewGraph,
            StateGraph jobResumeMatchGraph,
            FluxRegistry registry,
            JobMatchContext context,
            StreamContextRegistry contextRegistry,
            ExecutorService executorService,
            CompileConfig compileConfig,
            BaseCheckpointSaver checkpointSaver
    ) throws GraphStateException {
        this.graph = interviewGraph.compile(compileConfig);
        MemorySaver memorySaver = MemorySaver.builder().build();
        CompileConfig config = CompileConfig.builder()
                .saverConfig(SaverConfig.builder()
                        .register(memorySaver)
                        .build()
                ).build();
        this.matchGraph = jobResumeMatchGraph.compile(config);
        this.checkpointSaver = checkpointSaver;
        this.compileConfig = compileConfig;
        this.registry = registry;
        this.jobContext = context;
        this.contextRegistry = contextRegistry;
        this.executor = executorService;
    }

    @Override
    public void chat(
            Sinks.Many<ServerSentEvent<GraphNodeResponse>> sink,
            GraphRequest request
    ) {
        try {
            String chatId = request.getChatId();
            RunnableConfig config = RunnableConfig.builder()
                    .threadId(chatId)
                    .build();
            StreamContext context = contextRegistry.getOrCreate(chatId);
            context.setSink(sink);
            context.setChatId(chatId);
            boolean resuming = StrUtil.isNotBlank(request.getHumanFeedbackContent());
            if (resuming) {
                handleHumanFeedback(request, config);
            } else {
                handleProcess(context, request, config);
            }
        } catch (Exception e) {
            log.error("处理请求失败：{}", e.getMessage(), e);
            sink.tryEmitNext(ServerSentEvent.<GraphNodeResponse>builder()
                    .data(GraphNodeResponse.error(e.getMessage()))
                    .build());
        }
    }

    private void handleHumanFeedback(GraphRequest request, RunnableConfig config) throws Exception {
        Map<String, Object> stateUpdate = new HashMap<>();
        stateUpdate.put(HUMAN_FEEDBACK_DATA, Map.of(
                "approved", request.isApproved(),
                "feedback_content", request.getHumanFeedbackContent()));
        RunnableConfig updatedConfig = graph.updateState(config, stateUpdate);
        Flux<NodeOutput> flux = graph.stream(null, updatedConfig);
        subscribeToFlux(contextRegistry.get(request.getChatId()), flux, request);
    }

    private void handleProcess(StreamContext context, GraphRequest request, RunnableConfig config) throws IOException {
        if (context == null || context.getSink() == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "流式输出上下文不存在");
        }
        Map<String, Object> stateMap = new HashMap<>();
        MultipartFile file = request.getFile();
        String phase = request.getPhase();
        String input = request.getQuery();
        String chatId = request.getChatId();
        if (file != null && !file.isEmpty()) {
            String filePath = FileStorageUtil.saveTempFile(file,null,null,-1);
            stateMap.put(CURRENT_PHASE, INTERVIEW_PHASE);
            stateMap.put(INPUT_FILE, filePath);
        } else if (INTERVIEW_PHASE.equals(phase)) {
            stateMap.put(CURRENT_PHASE, INTERVIEW_PHASE);
            stateMap.put(USER_REPLY_ANSWER, input);
        } else {
            stateMap.put(INPUT_KEY, input);
        }
        stateMap.put(CHAT_ID, chatId);
        stateMap.put(USER_ID, request.getUserId());
        Flux<NodeOutput> nodeOutputFlux = graph.stream(stateMap, config);
        subscribeToFlux(context, nodeOutputFlux, request);
    }

    private void subscribeToFlux(StreamContext context, Flux<NodeOutput> nodeOutputFlux, GraphRequest request) {
        CompletableFuture.runAsync(() -> {
            Disposable disposable = nodeOutputFlux.subscribe(output -> handleOutput(request, output),
                    err -> handleError(request, err), () -> handleComplete(request));
            synchronized (context) {
                if (context.isCleaned()) {
                    // 如果已经清理，立即释放刚创建的 Disposable
                    if (disposable != null && !disposable.isDisposed()) {
                        disposable.dispose();
                    }
                } else {
                    // 只有在未清理的情况下才设置 Disposable
                    context.setDisposable(disposable);
                }
            }
        }, executor);
    }

    private void handleComplete(GraphRequest request) {
        String chatId = request.getChatId();
        StreamContext context = contextRegistry.get(chatId);
        int tokens = context.getTotalTokens().get();
        log.info("流式输出结束，chatId:{},totalTokens:{}", chatId, tokens);
        Sinks.Many<ServerSentEvent<GraphNodeResponse>> sink = context.getSink();
        if (sink != null && sink.currentSubscriberCount() > 0) {
            if (isAwaitingHumanFeedback(chatId)) {
                sink.tryEmitNext(ServerSentEvent.<GraphNodeResponse>builder()
                        .data(GraphNodeResponse.humanFeedback("是否将简历持久化到数据库"))
                        .build());
                sink.tryEmitComplete();
                return;
            }
            sink.tryEmitNext(ServerSentEvent.<GraphNodeResponse>builder()
                    .data(GraphNodeResponse.done(tokens))
                    .build());
            sink.tryEmitComplete();
        }
    }

    private boolean isAwaitingHumanFeedback(String chatId) {
        try {
            return checkpointSaver.get(RunnableConfig.builder().threadId(chatId).build())
                    .map(checkpoint -> HUMAN_FEEDBACK_NODE.equals(checkpoint.getNextNodeId()))
                    .orElse(false);
        } catch (Exception e) {
            log.warn("查询 checkpoint 失败，chatId: {}", chatId, e);
            return false;
        }
    }

    private void handleError(GraphRequest request, Throwable err) {
        String chatId = request.getChatId();
        log.error("流式输出错误，chatId:{},err:{}", chatId, err.getMessage(),err);
        StreamContext context = contextRegistry.get(chatId);
        Sinks.Many<ServerSentEvent<GraphNodeResponse>> sink = context.getSink();
        if (sink != null && sink.currentSubscriberCount() > 0) {
            sink.tryEmitNext(ServerSentEvent.<GraphNodeResponse>builder()
                    .data(GraphNodeResponse.error(err.getMessage()))
                    .build());
            sink.tryEmitComplete();
        }
        context.cleanup();
    }

    private void handleOutput(GraphRequest request, NodeOutput output) {
        String chatId = request.getChatId();
        StreamContext context = contextRegistry.get(chatId);
        if (context != null) {
            output.state()
                    .value(FINAL_ANSWER)
                    .map(Object::toString)
                    .filter(StrUtil::isNotBlank)
                    .ifPresent(context::setFinalAnswer);
        }
        if (output instanceof StreamingOutput streamingOutput) {
            handleStreamOutput(request, streamingOutput);
        }
    }

    private void handleStreamOutput(GraphRequest request, StreamingOutput streamingOutput) {
        String chatId = request.getChatId();
        StreamContext context = contextRegistry.get(chatId);
        if (context == null || context.getSink() == null) {
            log.info("流式输出中止，chatId:{}", chatId);
            return;
        }
        String node = streamingOutput.node();
        Sinks.Many<ServerSentEvent<GraphNodeResponse>> sink = context.getSink();

        switch (node) {
            case PARSE_RESUME_INFO_NODE -> {
                // 进度已由节点内部直接推送到 sink，此处无需额外处理
            }
            case ANSWER_WITH_RAG_NODE -> processAnswerWithRagNode(sink, streamingOutput);
            case SUMMARIZE_INTERVIEW_NODE -> processSummarizeInterviewNode(sink, streamingOutput);
            case GENERATE_QUESTION_NODE -> processGenerateQuestionNode(sink, streamingOutput);
        }
    }

    private void processSummarizeInterviewNode(Sinks.Many<ServerSentEvent<GraphNodeResponse>> sink, StreamingOutput output) {
        String fluxId = StateUtil.getStringValue(output.state(), FLUX_ID);
        Flux<GraphResponse<StreamingOutput>> flux = registry.get(fluxId);
        if (flux == null) {
            return;
        }
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
                String currentAnswer = JsonUtil.extractField(fullText, "summary");
                if (currentAnswer != null && currentAnswer.length() > lastExtracted[0].length()) {
                    String delta = currentAnswer.substring(lastExtracted[0].length());
                    if (!delta.isEmpty()) {
                        sink.tryEmitNext(ServerSentEvent.<GraphNodeResponse>builder()
                                .data(GraphNodeResponse.token(delta))
                                .build());
                        lastExtracted[0] = currentAnswer;
                    }
                }
            } catch (Exception e) {
                log.error("流式输出错误：", e);
                sink.tryEmitNext(ServerSentEvent.<GraphNodeResponse>builder()
                        .data(GraphNodeResponse.error(e.getMessage()))
                        .build());
            }
        }, err -> {
            log.error("流式输出出错，{}", err.getMessage());
            sink.tryEmitNext(ServerSentEvent.<GraphNodeResponse>builder()
                    .data(GraphNodeResponse.error(err.getMessage()))
                    .build());
            sink.tryEmitComplete();
        }, () -> {
            log.info("流式输出完成");
            sink.tryEmitComplete();
        });
    }

    private void processAnswerWithRagNode(Sinks.Many<ServerSentEvent<GraphNodeResponse>> sink, StreamingOutput output) {
        String fluxId = StateUtil.getStringValue(output.state(), FLUX_ID);
        Flux<GraphResponse<StreamingOutput>> flux = registry.get(fluxId);
        if (flux == null) {
            return;
        }
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
                String currentAnswer = JsonUtil.extractField(fullText, "answer");
                if (currentAnswer != null && currentAnswer.length() > lastExtracted[0].length()) {
                    String delta = currentAnswer.substring(lastExtracted[0].length());
                    if (!delta.isEmpty()) {
                        sink.tryEmitNext(ServerSentEvent.<GraphNodeResponse>builder()
                                .data(GraphNodeResponse.token(delta))
                                .build());
                        lastExtracted[0] = currentAnswer;
                    }
                }
            } catch (Exception e) {
                sink.tryEmitNext(ServerSentEvent.<GraphNodeResponse>builder()
                        .data(GraphNodeResponse.error(e.getMessage()))
                        .build());
            }
        }, err -> {
            log.error("流式输出错误", err);
            sink.tryEmitNext(ServerSentEvent.<GraphNodeResponse>builder()
                    .data(GraphNodeResponse.error(err.getMessage()))
                    .build());
        }, () -> {
            log.info("流式输出完成");
            sink.tryEmitComplete();
        });
    }

    private void processGenerateQuestionNode(Sinks.Many<ServerSentEvent<GraphNodeResponse>> sink, StreamingOutput output) {
        String fluxId = StateUtil.getStringValue(output.state(), FLUX_ID);
        String chatId = StateUtil.getStringValue(output.state(), CHAT_ID);
        Flux<GraphResponse<StreamingOutput>> flux = registry.get(fluxId);
        if (flux != null) {
            flux.subscribe(resp -> {
                try {
                    if (resp.getOutput() == null) {
                        resp.resultValue().ifPresent(value -> {
                            if (value instanceof Map<?, ?> map) {
                                Map<String, Object> update = new HashMap<>();
                                map.forEach((k, v) -> update.put(String.valueOf(k), v));
                                try {
                                    graph.updateState(
                                            RunnableConfig.builder()
                                                    .threadId(chatId)
                                                    .build(),
                                            update
                                    );
                                } catch (Exception e) {
                                    log.error("写入checkpoint失败", e);
                                }
                            }
                        });
                        return;
                    }
                    StreamingOutput streamingOutput = resp.getOutput().get();
                    String res = handleStreamStr(streamingOutput);
                    sink.tryEmitNext(ServerSentEvent.<GraphNodeResponse>builder()
                            .data(GraphNodeResponse.token(res))
                            .build());
                } catch (Exception e) {
                    log.error("处理流式响应失败", e);
                    sink.tryEmitNext(ServerSentEvent.<GraphNodeResponse>builder()
                            .data(GraphNodeResponse.error(e.getMessage()))
                            .build());
                }
            }, err -> {
                log.error("处理流式响应失败", err);
                sink.tryEmitNext(ServerSentEvent.<GraphNodeResponse>builder()
                        .data(GraphNodeResponse.error(err.getMessage()))
                        .build());
                sink.tryEmitComplete();
            }, () -> {
                log.info("流式响应完成");
                sink.tryEmitComplete();
            });
        }
    }


    @Override
    public Flux<ServerSentEvent<String>> evaluateAnswer(String chatId, String answer) {
        RunnableConfig config = RunnableConfig.builder()
                .threadId(chatId)
                .build();
        Map<String, Object> stateMap = new HashMap<>();
        stateMap.put(CHAT_ID, chatId);
        stateMap.put(USER_REPLY_ANSWER, answer);
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
        Map<String, Object> params = new HashMap<>();
        String jd = jobContext.get(JD_PREFIX + chatId);
        String resumePath = jobContext.get(RESUME_PREFIX + chatId);
        params.put(JOB_DESCRIPTION, jd);
        params.put(INPUT_FILE, resumePath);
        params.put(CHAT_ID, chatId);
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

    @Override
    public void stopStreamProcessing(String chatId) {
        StreamContext context = contextRegistry.get(chatId);
        if (context != null) {
            synchronized (context) {
                Disposable disposable = context.getDisposable();
                if (disposable != null && !disposable.isDisposed()) {
                    disposable.dispose();
                    log.info("已停止流式处理，chatId:{}", chatId);
                }
                context.cleanup();
            }
        } else {
            log.warn("未找到流式处理上下文，chatId:{}", chatId);
        }
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

}
