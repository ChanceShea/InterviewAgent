package com.shea.agent.interviewagent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author : Shea.
 * @since : 2026/7/22 17:13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String query;

    private Double threshold;

    private int topK = 3;
}
