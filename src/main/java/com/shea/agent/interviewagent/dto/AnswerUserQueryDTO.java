package com.shea.agent.interviewagent.dto;

import lombok.Data;

import java.util.List;

/**
 * @author : Shea.
 * @since : 2026/7/22 18:12
 */
@Data
public class AnswerUserQueryDTO {

    private String answer;

    private List<String> citations;

    private double confidence;
}
