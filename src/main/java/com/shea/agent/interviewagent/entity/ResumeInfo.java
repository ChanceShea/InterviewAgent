package com.shea.agent.interviewagent.entity;

import lombok.Data;

import java.util.List;

/**
 * @author : Shea.
 * @since : 2026/7/19 17:21
 */
@Data
public class ResumeInfo {

    String name;
    String job;
    java.util.List<String> skills;
    List<WorkExperience> workExperiences;
    List<Project> projects;
}
