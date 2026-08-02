package com.shea.agent.interviewagent.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author : Shea.
 * @since : 2026/8/2 14:59
 */
@Data
public class JobResumeMatchDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String jobType;

    private String matchScore;

    private String matchAnalysis;

    private String strengths;

    private String gaps;

    private String actionPlan;
}
