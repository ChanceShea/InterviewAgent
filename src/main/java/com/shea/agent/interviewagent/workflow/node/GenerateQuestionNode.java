package com.shea.agent.interviewagent.workflow.node;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.graph.GraphResponse;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.shea.agent.interviewagent.dto.InterviewQuestion;
import com.shea.agent.interviewagent.entity.ResumeInfo;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
        ResumeInfo info = StateUtil.getObjectValue(state, OUTPUT_INFO, ResumeInfo.class, null);
        String chatId = StateUtil.getStringValue(state, CHAT_ID);
        JSONObject jsonObject = JSONUtil.parseObj(info);
        String prompt = PromptHelper.buildGenerateQuestionPrompt(jsonObject);
        final Map<String, Object> resultMap = new HashMap<>();
        Flux<ChatResponse> response = streamLlmService
                .call(prompt, "根据简历内容生成面试问题")
                .doOnError(e -> log.error("生成面试问题失败，chatId={}", chatId,e));
        String fluxId = UUID.randomUUID().toString();
        Flux<GraphResponse<StreamingOutput>> generator = FluxUtil.createStreamingGenerator(
                this.getClass(), state, response,
                Flux.just(ChatResponseUtil.createResponse("正在生成面试问题")),
                Flux.just(ChatResponseUtil.createPureResponse("\n面试问题生成完毕")),
                r -> {
                    String questionText = extractQuestion(r);
                    InterviewQuestion question = InterviewQuestion.fromRaw(questionText);
                    log.info("生成面试问题：{}", question);
                    resultMap.put(QUESTIONS, question);
                    return resultMap;
                }
        );
        registry.addFlux(fluxId,generator);
        resultMap.put(FLUX_ID, fluxId);
        resultMap.put(CHAT_ID, chatId);
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
