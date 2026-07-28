package com.shea.agent.interviewagent.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author : Shea.
 * @since : 2026/7/25 10:10
 */
@Data
public class AnswerEvaluation {

    private String answer;

    private Evaluation evaluation;

    @JsonProperty("overall_comment")
    private String overallComment;

    @Data
    public static class Evaluation {
        @JsonProperty("technical_depth")
        private Score technicalDepth;
        @JsonProperty("problem_solving")
        private Score problemSolving;
        private Score communication;
        @JsonProperty("resume_consistency")
        private Score resumeConsistency;
    }

    @Data
    public static class Score {
        private double score;
    }
}
