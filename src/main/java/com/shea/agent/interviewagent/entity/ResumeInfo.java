package com.shea.agent.interviewagent.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author : Shea.
 * @since : 2026/7/19 17:21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeInfo {

    String name;
    String job;
    java.util.List<String> skills;
    List<WorkExperience> workExperiences;
    List<Project> projects;
}
