package com.shea.agent.interviewagent.workflow.node;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.graph.GraphResponse;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.shea.agent.interviewagent.dto.ResumeInfoDTO;
import com.shea.agent.interviewagent.entity.HistoryMessage;
import com.shea.agent.interviewagent.prompt.PromptHelper;
import com.shea.agent.interviewagent.registry.FluxRegistry;
import com.shea.agent.interviewagent.service.LlmService;
import com.shea.agent.interviewagent.utils.FluxUtil;
import com.shea.agent.interviewagent.utils.StateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.shea.agent.interviewagent.constant.Constant.*;

/**
 * 生成问题节点，用于LLM解析完简历后，根据简历生成相应的面试问题
 * @author : Shea.
 * @since : 2026/7/20 14:45
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class GenerateQuestionNode implements NodeAction {

    private final LlmService streamLlmService;
    private final FluxRegistry registry;

    private static final Pattern QUESTION_PATTERN = Pattern.compile("<question>(.*?)</question>", Pattern.DOTALL);

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        ResumeInfoDTO info = StateUtil.getObjectValue(state, OUTPUT_INFO, ResumeInfoDTO.class, null);
        String chatId = StateUtil.getStringValue(state, CHAT_ID);
        String multiTurn = StateUtil.getStringValue(state, MULTI_TURN,"(无)");
        String prompt = PromptHelper.buildGenerateQuestionPrompt(info,multiTurn);
        final Map<String, Object> resultMap = new HashMap<>();
        Flux<ChatResponse> response = streamLlmService
                .call(prompt, "根据简历内容生成面试问题")
                .doOnError(e -> log.error("生成面试问题失败，chatId={}", chatId,e));
        String fluxId = UUID.randomUUID().toString();
        Flux<GraphResponse<StreamingOutput>> generator = FluxUtil.createStreamingGenerator(
                this.getClass(), state, response, Flux.just(), Flux.just(),
                r -> getResultMap(r, multiTurn, chatId, resultMap)
        );
        registry.addFlux(fluxId,generator);
        resultMap.put(FLUX_ID, fluxId);
        resultMap.put(CHAT_ID, chatId);
        return resultMap;
    }

    @NotNull
    private Map<String, Object> getResultMap(String r, String multiTurn, String chatId, Map<String, Object> resultMap) {
        String questionText = extractQuestion(r);
//                    InterviewQuestion question = InterviewQuestion.fromRaw(questionText);
        log.info("生成面试问题：{}", questionText);
        List<HistoryMessage> histories;
        if ("(无)".equals(multiTurn)) {
            histories = new ArrayList<>();
        } else {
            histories = JSONUtil.toList(multiTurn, HistoryMessage.class);
        }
        HistoryMessage message = HistoryMessage.builder()
                .chatId(chatId)
                .message(questionText)
                .messageType(ASSISTANT_MESSAGE)
                .build();
        histories.add(message);
        resultMap.put(FINAL_ANSWER, questionText);
        resultMap.put(MULTI_TURN, JSONUtil.toJsonStr(histories));
        return resultMap;
    }

    private String extractQuestion(String raw) {
        if (StrUtil.isBlank(raw)) {
            return "";
        }
        Matcher matcher = QUESTION_PATTERN.matcher(raw);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return raw.trim();
    }
}
