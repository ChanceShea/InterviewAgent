package com.shea.agent.interviewagent.workflow.node;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.shea.agent.interviewagent.dto.QueryRewriteDTO;
import com.shea.agent.interviewagent.prompt.PromptHelper;
import com.shea.agent.interviewagent.service.LlmService;
import com.shea.agent.interviewagent.utils.ChatResponseUtil;
import com.shea.agent.interviewagent.utils.StateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

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

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String question = StateUtil.getStringValue(state, INPUT_KEY);
        String chatId = StateUtil.getStringValue(state, CHAT_ID);
        log.info("增强前问题：{}，chatId：{}", question,chatId);
        String multiTurn = StateUtil.getStringValue(state, MULTI_TURN_QUERY, "(无)");
        String prompt = PromptHelper.buildEnhanceUserPrompt(multiTurn, question);
        StringBuilder sb = new StringBuilder();
        streamLlmServiceImpl.callUser(prompt)
                        .doOnNext(resp -> sb.append(ChatResponseUtil.getText(resp)))
                                .blockLast();
        String raw = sb.toString();
        String standaloneQuery = extractStandaloneQuery(raw);
        return Map.of(
                ENHANCED_QUERY,standaloneQuery,
                CHAT_ID,chatId
        );
    }

    private String extractStandaloneQuery(String rawQuery) {
        QueryRewriteDTO bean = JSONUtil.toBean(rawQuery, QueryRewriteDTO.class);
        return bean.getStandaloneQuery();
    }
}
