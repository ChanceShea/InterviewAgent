package com.shea.agent.interviewagent.workflow.node;

import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.shea.agent.interviewagent.dto.AnswerEvaluation;
import com.shea.agent.interviewagent.dto.ResumeInfoDTO;
import com.shea.agent.interviewagent.prompt.PromptHelper;
import com.shea.agent.interviewagent.service.LlmService;
import com.shea.agent.interviewagent.utils.StateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.shea.agent.interviewagent.constant.Constant.*;

/**
 * @author : Shea.
 * @since : 2026/7/28 10:08
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PlannerNode implements NodeAction {

    private final LlmService streamLlmServiceImpl;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        ResumeInfoDTO resumeInfo = StateUtil.getObjectValue(state, OUTPUT_INFO, ResumeInfoDTO.class);
        List<AnswerEvaluation> evaluations = StateUtil.getListValue(state, EVALUATIONS, AnswerEvaluation.class, new ArrayList<>());
        String prompt = PromptHelper.buildInterviewPlannerPrompt(resumeInfo, evaluations);
        Flux<ChatResponse> flux = streamLlmServiceImpl.callUser(prompt);
        String nextStep = flux.mapNotNull(resp -> resp.getResult().getOutput().getText())
                .filter(StrUtil::isNotBlank)
                .collect(StringBuilder::new, StringBuilder::append)
                .map(StringBuilder::toString)
                .block();
        String rawNextStep = nextStep;
        nextStep = extractAnswer(nextStep);
        log.info("PlannerNode LLM原始输出: {}", rawNextStep);
        log.info("PlannerNode 提取后的nextStep: {}", nextStep);
        return Map.of(
                NEXT_STEP,nextStep
        );
    }

    private String extractAnswer(String input) {
        Pattern pattern = Pattern.compile("<verdict>(.*?)</verdict>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return input;
    }
}
