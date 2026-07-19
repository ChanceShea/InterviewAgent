package com.shea.agent.interviewagent.prompt;

/**
 * @author : Shea.
 * @since : 2026/7/19 17:53
 */
public class PromptHelper {

    public static String buildParseResumeInfoPrompt() {
        return PromptConstant.getParseResumeInfoPrompt().render();
    }
}
