package com.shea.agent.interviewagent.workflow.node;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.shea.agent.interviewagent.dto.ResumeInfoDTO;
import com.shea.agent.interviewagent.prompt.PromptHelper;
import com.shea.agent.interviewagent.registry.StreamContextRegistry;
import com.shea.agent.interviewagent.service.LlmService;
import com.shea.agent.interviewagent.utils.FileStorageUtil;
import com.shea.agent.interviewagent.utils.StateUtil;
import com.shea.agent.interviewagent.vo.GraphNodeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.shea.agent.interviewagent.constant.Constant.*;

/**
 * @author : Shea.
 * @since : 2026/7/19 16:48
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ParseResumeInfoNode implements NodeAction {

    private final LlmService streamLlmService;
    private final StreamContextRegistry contextRegistry;
    private final Cache<String, ResumeInfoDTO> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(1024)
            .maximumSize(10_000L) // 最大缓存数量
            .expireAfterWrite(Duration.ofMinutes(10)) // 缓存过期时间
            .build();

    private static final String[] PROGRESS_MESSAGES = {
            "正在解析简历...",
            "正在提取基本信息...",
            "正在分析工作经历...",
            "正在分析项目经验...",
            "正在整理技能标签...",
            "即将完成..."
    };

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String filePath = StateUtil.getStringValue(state, INPUT_FILE);
        String chatId = StateUtil.getStringValue(state, CHAT_ID);
        Sinks.Many<ServerSentEvent<GraphNodeResponse>> sink = contextRegistry.get(chatId) != null
                ? contextRegistry.get(chatId).getSink()
                : null;

        // ---- 子阶段1：读取PDF ----
        emitStage(sink, "正在读取PDF文件...");
        List<Document> documents = loadDocument(filePath);
        String resumeText = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining());

        // ---- 子阶段2：缓存查询 ----
        String md5Hash = DigestUtil.md5Hex(resumeText);
        ResumeInfoDTO info;
        if ((info = LOCAL_CACHE.getIfPresent(md5Hash)) != null) {
            log.info("缓存命中，跳过解析：{}", info);
            emitStage(sink, "简历解析完成（缓存）");
            emitHumanFeedback(sink, "是否将简历持久化到数据库");
            return Map.of(OUTPUT_INFO, info, CHAT_ID, chatId, INPUT_FILE, "");
        }

        // ---- 子阶段3：LLM解析（定时推送假进度） ----
        String prompt = PromptHelper.buildParseResumeInfoPrompt();
        Flux<ChatResponse> response = streamLlmService.call(prompt, resumeText, ResumeInfoDTO.class);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        AtomicBoolean running = new AtomicBoolean(true);
        final int[] msgIndex = {0};

        scheduler.scheduleAtFixedRate(() -> {
            if (running.get() && sink != null && msgIndex[0] < PROGRESS_MESSAGES.length) {
                sink.tryEmitNext(ServerSentEvent.<GraphNodeResponse>builder()
                        .data(GraphNodeResponse.think(PROGRESS_MESSAGES[msgIndex[0]]))
                        .build());
                msgIndex[0]++;
            }
        }, 0, 7, TimeUnit.SECONDS);

        try {
            info = response.mapNotNull(
                            r -> r.getResult().getOutput().getText()
                    ).collect(StringBuilder::new, StringBuilder::append)
                    .map(StringBuilder::toString)
                    .filter(StrUtil::isNotBlank)
                    .map(s -> JSONUtil.toBean(s, ResumeInfoDTO.class))
                    .block();
        } finally {
            running.set(false);
            scheduler.shutdown();
            FileStorageUtil.deleteTempFile(filePath);
        }

        log.info("成功解析简历：{}", info);
        LOCAL_CACHE.put(md5Hash, info);

        // ---- 子阶段4：完成 ----
        emitStage(sink, "简历解析完成");
        emitHumanFeedback(sink, "是否将简历持久化到数据库");

        return Map.of(OUTPUT_INFO, info, CHAT_ID, chatId, INPUT_FILE, "");
    }

    private void emitStage(Sinks.Many<ServerSentEvent<GraphNodeResponse>> sink, String stage) {
        if (sink != null) {
            sink.tryEmitNext(ServerSentEvent.<GraphNodeResponse>builder()
                    .data(GraphNodeResponse.think(stage))
                    .build());
        }
    }

    private void emitHumanFeedback(Sinks.Many<ServerSentEvent<GraphNodeResponse>> sink, String message) {
        if (sink != null) {
            sink.tryEmitNext(ServerSentEvent.<GraphNodeResponse>builder()
                    .data(GraphNodeResponse.humanFeedback(message))
                    .build());
        }
    }

    private List<Document> loadDocument(String filePath) {
        Resource resource = new FileSystemResource(filePath);
        PagePdfDocumentReader reader = new PagePdfDocumentReader(resource);
        return reader.read();
    }
}
