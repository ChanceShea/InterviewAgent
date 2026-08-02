package com.shea.agent.interviewagent.workflow.node;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.shea.agent.interviewagent.entity.ResumeInfo;
import com.shea.agent.interviewagent.prompt.PromptHelper;
import com.shea.agent.interviewagent.registry.FluxRegistry;
import com.shea.agent.interviewagent.service.LlmService;
import com.shea.agent.interviewagent.utils.StateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;

import static com.shea.agent.interviewagent.constant.Constant.*;

/**
 * @author : Shea.
 * @since : 2026/7/31 09:52
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JobResumeMatchNode implements NodeAction {

    private final LlmService streamLlmServiceImpl;
    private final FluxRegistry registry;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String jd = StateUtil.getStringValue(state, JOB_DESCRIPTION);
        ResumeInfo info = StateUtil.getObjectValue(state, OUTPUT_INFO, ResumeInfo.class);
        String infoStr = JSONUtil.toJsonStr(info);
        String prompt = PromptHelper.buildJobResumeMatchPrompt(jd, infoStr);
        Flux<ChatResponse> flux = streamLlmServiceImpl.callUser(prompt);
        String result = flux.mapNotNull(r -> r.getResult().getOutput().getText())
                .collect(StringBuilder::new, StringBuilder::append)
                .map(StringBuilder::toString)
                .block();
        return Map.of(
                MATCH_RESULT,result
        );
    }
}
