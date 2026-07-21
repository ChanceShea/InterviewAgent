package com.shea.agent.interviewagent.prompt;

import com.shea.agent.interviewagent.dto.QueryRewriteDTO;
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
}
