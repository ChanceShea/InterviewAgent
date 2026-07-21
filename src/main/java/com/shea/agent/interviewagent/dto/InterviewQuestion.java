package com.shea.agent.interviewagent.dto;

import lombok.Data;

/**
 * @author : Shea.
 * @since : 2026/7/20 10:00
 */
@Data
public class InterviewQuestion {

    String question;

    public static InterviewQuestion fromRaw(String raw) {
        InterviewQuestion q = new InterviewQuestion();
        q.setQuestion(raw);
        return q;
    }
}
