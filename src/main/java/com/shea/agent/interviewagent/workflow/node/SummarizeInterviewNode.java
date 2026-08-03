package com.shea.agent.interviewagent.workflow.node;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.graph.GraphResponse;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.shea.agent.interviewagent.dto.AnswerEvaluation;
import com.shea.agent.interviewagent.entity.ResumeInfo;
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

import java.util.*;

import static com.shea.agent.interviewagent.constant.Constant.*;

/**
 * @author : Shea.
 * @since : 2026/7/30 14:21
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SummarizeInterviewNode implements NodeAction {

    private final LlmService streamLlmServiceImpl;
    private final FluxRegistry registry;
    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        List<AnswerEvaluation> evaluationList = StateUtil.getListValue(state, EVALUATIONS, AnswerEvaluation.class,new ArrayList<>());
        ResumeInfo resumeInfo = StateUtil.getObjectValue(state, OUTPUT_INFO,ResumeInfo.class,null);
        String evaluations = JSONUtil.toJsonStr(evaluationList);
        String prompt = PromptHelper.buildSummarizeInterviewPrompt(resumeInfo, evaluations);
        Flux<ChatResponse> flux = streamLlmServiceImpl.callUser(prompt);
        Map<String,Object> resultMap = new HashMap<>();
        Flux<GraphResponse<StreamingOutput>> generator = FluxUtil.createStreamingGenerator(
                this.getClass(), state, flux,
                Flux.empty(), Flux.empty(), r -> {
                    log.info("Summarize Interview Node输出结果:" + r);
                    resultMap.put(FINAL_ANSWER, r);
                    return resultMap;
                });
        String fluxId = UUID.randomUUID().toString();
        registry.addFlux(fluxId, generator);
        resultMap.put(FLUX_ID, fluxId);
        return resultMap;
    }
}
