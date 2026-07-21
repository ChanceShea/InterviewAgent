package com.shea.agent.interviewagent.controller;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.GraphResponse;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.shea.agent.interviewagent.registry.FluxRegistry;
import com.shea.agent.interviewagent.utils.FileStorageUtil;
import com.shea.agent.interviewagent.utils.StateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.shea.agent.interviewagent.constant.Constant.*;

/**
 * @author : Shea.
 * @since : 2026/7/20 17:44
 */
@RestController
@RequestMapping("/ai")
@Slf4j
public class AiController {

    private final CompiledGraph graph;
    private final FluxRegistry registry;

    public AiController(StateGraph interviewGraph,FluxRegistry registry) throws GraphStateException {
        this.graph = interviewGraph.compile();
        this.registry = registry;
    }

    @PostMapping(value = "/upload",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("chatId") String chatId
    ) {
        String filePath;
        try {
            filePath = FileStorageUtil.saveTempFile(file);
            RunnableConfig config = RunnableConfig.builder()
                    .threadId(chatId)
                    .build();
            Map<String,Object> stateMap = new HashMap<>();
            stateMap.put(INPUT_FILE,filePath);
            stateMap.put(CHAT_ID,chatId);
            return graph.stream(stateMap,config)
                    .flatMap(content -> {
                        if (content instanceof StreamingOutput output) {
                            if (GENERATE_QUESTION_NODE.equals(output.node())) {
                                String fluxId = StateUtil.getStringValue(output.state(), FLUX_ID);
                                Flux<GraphResponse<StreamingOutput>> flux = registry.get(fluxId);
                                if (flux != null) {
                                    return flux.flatMap(resp -> {
                                        try {
                                            if (resp.getOutput() == null) {
                                                return Flux.empty();
                                            }
                                            StreamingOutput streamingOutput = resp.getOutput().get();
                                            String resultStr = streamingOutput.getOriginData().toString();
                                            Pattern pattern = Pattern.compile("textContent=(.*?), metadata=",Pattern.DOTALL);
                                            Matcher matcher = pattern.matcher(resultStr);
                                            if (matcher.find()) {
                                                return Flux.just(ServerSentEvent.<String>builder()
                                                        .data(matcher.group(1))
                                                        .build());
                                            }
                                        } catch (Exception e) {
                                            log.error("处理流式响应失败", e);
                                            return Flux.just(ServerSentEvent.<String>builder()
                                                    .data("流式响应处理失败，请稍后再试")
                                                    .build());
                                        }
                                        return Flux.just(ServerSentEvent.<String>builder()
                                                .data("AI输出失败，请稍后再试")
                                                .build());
                                    });
                                }
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
}
