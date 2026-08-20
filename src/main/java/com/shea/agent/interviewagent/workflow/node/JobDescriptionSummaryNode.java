package com.shea.agent.interviewagent.workflow.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.shea.agent.interviewagent.prompt.PromptHelper;
import com.shea.agent.interviewagent.registry.FluxRegistry;
import com.shea.agent.interviewagent.service.LlmService;
import com.shea.agent.interviewagent.utils.StateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.shea.agent.interviewagent.constant.Constant.JOB_DESCRIPTION;

/**
 * @author : Shea.
 * @since : 2026/7/30 19:36
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JobDescriptionSummaryNode implements NodeAction {

    private final LlmService streamLlmServiceImpl;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String jd = StateUtil.getStringValue(state, JOB_DESCRIPTION, "");
        String prompt = PromptHelper.buildJobDescriptionSummaryPrompt(jd);
        Flux<ChatResponse> flux = streamLlmServiceImpl.callUser(prompt);
        String summary = flux.mapNotNull(r -> r.getResult().getOutput().getText())
                .collect(StringBuilder::new, StringBuilder::append)
                .map(StringBuilder::toString)
                .block();
        return Map.of(JOB_DESCRIPTION, summary);
    }
}
