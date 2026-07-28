package com.shea.agent.interviewagent.prompt;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.shea.agent.interviewagent.dto.AnswerUserQueryDTO;
import com.shea.agent.interviewagent.dto.QueryRewriteDTO;
import com.shea.agent.interviewagent.entity.ResumeInfo;
import org.springframework.ai.converter.BeanOutputConverter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : Shea.
 * @since : 2026/7/19 17:53
 */
public class PromptHelper {

    public static String buildParseResumeInfoPrompt() {
        return PromptConstant.getParseResumeInfoPrompt().render();
    }

    public static String buildGenerateQuestionPrompt(Map<String,Object> resumeInfo) {
        return PromptConstant.getGenerateQuestionPrompt().render(resumeInfo);
    }

    public static String buildEnhanceUserPrompt(String multiTurn,String latestQuery) {
        Map<String,Object> params = new HashMap<>();
        params.put("multi_turn", multiTurn != null ? multiTurn : "(无)");
        params.put("latest_query", latestQuery);
        BeanOutputConverter<QueryRewriteDTO> beanOutputConverter = new BeanOutputConverter<>(QueryRewriteDTO.class);
        params.put("format", beanOutputConverter.getFormat());
        return PromptConstant.getEnhanceUserPrompt().render(params);
    }

    public static String buildAnswerWithRagPrompt(String documents,String multiTurn,String enhancedQuery) {
        Map<String,Object> params = new HashMap<>();
        params.put("documents", documents);
        params.put("multi_turn", multiTurn);
        params.put("enhanced_query", enhancedQuery);
        BeanOutputConverter<AnswerUserQueryDTO> beanOutputConverter = new BeanOutputConverter<>(AnswerUserQueryDTO.class);
        params.put("format", beanOutputConverter.getFormat());
        return PromptConstant.getAnswerWithRagPrompt().render(params);
    }

    public static String buildEvaluateUserAnswerPrompt(ResumeInfo info,String currentQuestion,String userAnswer,String multiTurn) {
        JSONObject resume = JSONUtil.parseObj(info);
        Map<String, Object> params = new HashMap<>(resume);
        params.put("current_question", currentQuestion);
        params.put("user_answer", userAnswer);
        params.put("multi_turn", multiTurn);
        return PromptConstant.getEvaluateUserAnswerPrompt().render(params);
    }
}
