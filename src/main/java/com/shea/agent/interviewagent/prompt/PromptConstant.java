package com.shea.agent.interviewagent.prompt;

import org.springframework.ai.chat.prompt.PromptTemplate;

/**
 * @author : Shea.
 * @since : 2026/7/19 17:52
 */
public class PromptConstant {

    private PromptConstant() {}


    public static PromptTemplate getParseResumeInfoPrompt() {
        return new PromptTemplate(PromptLoader.loadPrompt("parse-resume-info.txt"));
    }
}
