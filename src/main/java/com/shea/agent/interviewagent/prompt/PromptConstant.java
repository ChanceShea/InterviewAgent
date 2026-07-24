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

    public static PromptTemplate getGenerateQuestionPrompt() {
        return new PromptTemplate(PromptLoader.loadPrompt("generate-question.txt"));
    }

    public static PromptTemplate getEnhanceUserPrompt() {
        return new PromptTemplate(PromptLoader.loadPrompt("enhance-user-query.txt"));
    }

    public static PromptTemplate getAnswerWithRagPrompt() {
        return new PromptTemplate(PromptLoader.loadPrompt("answer-with-rag.txt"));
    }
}
