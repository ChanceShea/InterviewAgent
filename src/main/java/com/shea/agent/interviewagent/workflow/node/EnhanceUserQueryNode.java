package com.shea.agent.interviewagent.workflow.node;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.graph.GraphResponse;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.shea.agent.interviewagent.dto.QueryRewriteDTO;
import com.shea.agent.interviewagent.prompt.PromptHelper;
import com.shea.agent.interviewagent.registry.FluxRegistry;
import com.shea.agent.interviewagent.service.LlmService;
import com.shea.agent.interviewagent.utils.ChatResponseUtil;
import com.shea.agent.interviewagent.utils.FluxUtil;
import com.shea.agent.interviewagent.utils.StateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.shea.agent.interviewagent.constant.Constant.*;

/**
 * 对用户的问题进行增强
 * @author : Shea.
 * @since : 2026/7/21 21:20
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EnhanceUserQueryNode implements NodeAction {

    private final LlmService streamLlmServiceImpl;
    private final FluxRegistry registry;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String question = StateUtil.getStringValue(state, INPUT_KEY);
        String chatId = StateUtil.getStringValue(state, CHAT_ID);
        log.info("增强前问题：{}，chatId：{}", question,chatId);
        String multiTurn = StateUtil.getStringValue(state, MULTI_TURN_QUERY, "(无)");
        String prompt = PromptHelper.buildEnhanceUserPrompt(multiTurn, question);
        Flux<ChatResponse> responseFlux = streamLlmServiceImpl.callUser(prompt);
        Sinks.Many<String> queryDisplaySink = Sinks.many().multicast().onBackpressureBuffer();
        final Map<String,Object> resultMap = new HashMap<>();
        String fluxId = UUID.randomUUID().toString();
        Flux<GraphResponse<StreamingOutput>> generator = FluxUtil.createStreamingGenerator(this.getClass(), state, responseFlux,
                Flux.just(ChatResponseUtil.createResponse("正在构建增强Query")),
                Flux.just(ChatResponseUtil.createPureResponse("\n构建增强Query完成")),
                res -> {
                    resultMap.putAll(getQueries(res, chatId, queryDisplaySink));
                    return resultMap;
                });
        registry.addFlux(fluxId,generator);
        resultMap.put(FLUX_ID, fluxId);
        resultMap.put(CHAT_ID, chatId);
        return resultMap;
    }

    private Map<String, Object> getQueries(String res, String chatId, Sinks.Many<String> sink) {
        try {
            String standaloneQuery = extractStandaloneQuery(res);
            if (standaloneQuery == null || standaloneQuery.isEmpty()) {
                log.debug("查询增强Query失败");
                sink.tryEmitNext("未能进行查询重写！\n");
                return Map.of(ENHANCED_QUERY,res);
            }
            return Map.of(ENHANCED_QUERY,standaloneQuery);
        }catch (Exception e){
            log.error("查询增强错误");
            sink.tryEmitError(e);
            return Map.of(ENHANCED_QUERY,"");
        } finally {
            sink.tryEmitComplete();
        }
    }

    private String extractStandaloneQuery(String rawQuery) {
        QueryRewriteDTO bean = JSONUtil.toBean(rawQuery, QueryRewriteDTO.class);
        return bean.getStandaloneQuery();
    }
}
