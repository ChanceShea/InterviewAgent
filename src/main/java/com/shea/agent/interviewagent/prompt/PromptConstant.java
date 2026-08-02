package com.shea.agent.interviewagent.prompt;

import org.springframework.ai.chat.prompt.PromptTemplate;

/**
 * @author : Shea.
 * @since : 2026/7/19 17:52
 */
public final class PromptConstant {

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

    public static PromptTemplate getEvaluateUserAnswerPrompt() {
        return new PromptTemplate(PromptLoader.loadPrompt("evaluate-user-answer.txt"));
    }

    public static PromptTemplate getInterviewPlannerPrompt() {
        return new PromptTemplate(PromptLoader.loadPrompt("interview-planner.txt"));
    }

    public static PromptTemplate getSummarizeInterviewPrompt() {
        return new PromptTemplate(PromptLoader.loadPrompt("summarize-interview.txt"));
    }

    public static PromptTemplate getJobDescriptionSummaryPrompt() {
        return new PromptTemplate(PromptLoader.loadPrompt("job-description-summary.txt"));
    }

    public static PromptTemplate getJobResumeMatchPrompt() {
        return new PromptTemplate(PromptLoader.loadPrompt("job-resume-match.txt"));
    }
}
