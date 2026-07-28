package com.shea.agent.interviewagent.workflow.node;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.shea.agent.interviewagent.dto.AnswerEvaluation;
import com.shea.agent.interviewagent.dto.InterviewQuestion;
import com.shea.agent.interviewagent.entity.ResumeInfo;
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

import static com.shea.agent.interviewagent.constant.Constant.*;

/**
 * @author : Shea.
 * @since : 2026/7/25 09:39
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EvaluateUserAnswerNode implements NodeAction {

    private final LlmService streamLlmService;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String userAnswer = StateUtil.getStringValue(state, USER_REPLY_ANSWER);
        ResumeInfo info = StateUtil.getObjectValue(state, OUTPUT_INFO, ResumeInfo.class, null);
        InterviewQuestion interviewQuestion = StateUtil.getObjectValue(state, QUESTION, InterviewQuestion.class, null);
        String questions = interviewQuestion.getQuestion();
        String multiTurn = StateUtil.getStringValue(state, MULTI_TURN_QUERY, "(无)");
        String chatId = StateUtil.getStringValue(state, CHAT_ID);
        List<AnswerEvaluation> evaluations = StateUtil.getListValue(state, EVALUATIONS, AnswerEvaluation.class, new ArrayList<>());
        String prompt = PromptHelper.buildEvaluateUserAnswerPrompt(info, userAnswer, questions, multiTurn);
        log.info("构建好的prompt：\n {}",prompt);
        Flux<ChatResponse> flux = streamLlmService.callUser(prompt,AnswerEvaluation.class);
        AnswerEvaluation evaluation = flux.mapNotNull(resp -> resp.getResult().getOutput().getText())
                .collect(StringBuilder::new, StringBuilder::append)
                .map(StringBuilder::toString)
                .filter(StrUtil::isNotBlank)
                .map(s -> JSONUtil.toBean(s, AnswerEvaluation.class))
                .block();
        evaluations.add(evaluation);
        return Map.of(
                EVALUATIONS,evaluations,
                CHAT_ID,chatId
        );
    }
}
