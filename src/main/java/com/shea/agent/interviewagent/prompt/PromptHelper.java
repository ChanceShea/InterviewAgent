package com.shea.agent.interviewagent.prompt;

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
}
