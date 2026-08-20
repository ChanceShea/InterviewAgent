package com.shea.agent.interviewagent.workflow.node;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.graph.GraphResponse;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.shea.agent.interviewagent.dto.AgentQueryDTO;
import com.shea.agent.interviewagent.dto.AnswerUserQueryDTO;
import com.shea.agent.interviewagent.entity.HistoryMessage;
import com.shea.agent.interviewagent.prompt.PromptHelper;
import com.shea.agent.interviewagent.registry.FluxRegistry;
import com.shea.agent.interviewagent.service.AgentKnowledgeService;
import com.shea.agent.interviewagent.service.LlmService;
import com.shea.agent.interviewagent.utils.FluxUtil;
import com.shea.agent.interviewagent.utils.StateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

import static com.shea.agent.interviewagent.constant.Constant.*;

/**
 * @author : Shea.
 * @since : 2026/7/22 17:33
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AnswerWithRagNode implements NodeAction {

    private final AgentKnowledgeService agentKnowledgeService;
    private final LlmService streamLlmService;
    private final FluxRegistry fluxRegistry;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String enhancedQuery = StateUtil.getStringValue(state, ENHANCED_QUERY);
        String multiTurn = StateUtil.getStringValue(state, MULTI_TURN, "(无)");
        String chatId = StateUtil.getStringValue(state, CHAT_ID);
        log.info("增强后查询：{}",enhancedQuery);
        AgentQueryDTO dto = AgentQueryDTO.builder()
                .query(enhancedQuery)
                .threshold(0.5)
                .topK(5)
                .build();
        List<HistoryMessage> histories;
        if ("(无)".equals(multiTurn)) {
            histories = new ArrayList<>();
        } else {
            histories = JSONUtil.toList(multiTurn, HistoryMessage.class);
        }
        HistoryMessage userMessage = HistoryMessage.builder()
                .chatId(chatId)
                .messageType(USER_MESSAGE)
                .message(enhancedQuery)
                .build();
        histories.add(userMessage);
        List<Document> documents = agentKnowledgeService.queryDocuments(dto);
        String allDocuments = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining(","));
        String prompt = PromptHelper.buildAnswerWithRagPrompt(allDocuments, multiTurn, enhancedQuery);
        Flux<ChatResponse> responseFlux = streamLlmService.callUser(prompt);
        Map<String,Object> resultMap = new HashMap<>();
        String fluxId = UUID.randomUUID().toString();
        Flux<GraphResponse<StreamingOutput>> generator = FluxUtil.createStreamingGenerator(this.getClass(), state, responseFlux,
                Flux.empty(),
                Flux.empty(),
                res -> {
                    AnswerUserQueryDTO answerUserQueryDTO = JSONUtil.toBean(res, AnswerUserQueryDTO.class);
                    HistoryMessage message = HistoryMessage.builder()
                            .messageType(ASSISTANT_MESSAGE)
                            .chatId(chatId)
                            .message(answerUserQueryDTO.getAnswer())
                            .build();
                    histories.add(message);
                    resultMap.put(FINAL_ANSWER, answerUserQueryDTO.getAnswer());
                    resultMap.put(MULTI_TURN, JSONUtil.toJsonStr(histories));
                    log.info("AI生成回复：{}",answerUserQueryDTO.getAnswer());
                    return resultMap;
                });
        fluxRegistry.addFlux(fluxId, generator);
        resultMap.put(CHAT_ID,chatId);
        resultMap.put(FLUX_ID,fluxId);
        return resultMap;
    }
}
