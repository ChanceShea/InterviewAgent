package com.shea.agent.interviewagent.dto;

import com.shea.agent.interviewagent.entity.ResumeInfo;
import lombok.Data;

import java.util.List;

/**
 * 面试问题上下文对象
 * @author : Shea.
 * @since : 2026/7/20 15:45
 */
@Data
public class InterviewContext {

    private ResumeInfo info;

    private List<String> askedQuestions;

    private String currentPhase;

    private int totalRounds;

    private int currentRound;
}
