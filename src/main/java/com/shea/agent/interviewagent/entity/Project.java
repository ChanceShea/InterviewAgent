package com.shea.agent.interviewagent.entity;

import lombok.Data;

import java.util.List;

/**
 * @author : Shea.
 * @since : 2026/7/19 17:22
 */
@Data
public class Project {
    String projectName;
    String duration;
    List<String> techStack;
    String description;
}
