package com.shea.agent.interviewagent.workflow.node;

import com.alibaba.cloud.ai.graph.GraphResponse;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.shea.agent.interviewagent.prompt.PromptHelper;
import com.shea.agent.interviewagent.registry.FluxRegistry;
import com.shea.agent.interviewagent.service.LlmService;
import com.shea.agent.interviewagent.utils.FluxUtil;
import com.shea.agent.interviewagent.utils.StateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.shea.agent.interviewagent.constant.Constant.FLUX_ID;
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
    private final FluxRegistry registry;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String jd = StateUtil.getStringValue(state, JOB_DESCRIPTION, "");
        String prompt = PromptHelper.buildJobDescriptionSummaryPrompt(jd);
        String fluxId = UUID.randomUUID().toString();
        Flux<ChatResponse> flux = streamLlmServiceImpl.callUser(prompt);
        Map<String,Object> resultMap = new HashMap<>();
        Flux<GraphResponse<StreamingOutput>> generator = FluxUtil.createStreamingGenerator(
                this.getClass(), state, flux,
                Flux.empty(), Flux.empty(),
                r -> {
                    resultMap.put(JOB_DESCRIPTION, r);
                    return resultMap;
                });
        registry.addFlux(fluxId,generator);
        resultMap.put(FLUX_ID,fluxId);
        return resultMap;
    }
}
