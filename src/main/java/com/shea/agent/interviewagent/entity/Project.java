package com.shea.agent.interviewagent.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author : Shea.
 * @since : 2026/7/19 17:22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    String projectName;
    String duration;
    List<String> techStack;
    String description;
}
