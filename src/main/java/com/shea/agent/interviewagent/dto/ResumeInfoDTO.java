package com.shea.agent.interviewagent.dto;

import com.shea.agent.interviewagent.entity.ResumeInfo;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @author : Shea.
 * @since : 2026/8/5 09:19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeInfoDTO implements Serializable {

    private String name;
    private String job;

    private List<String> skills;

    private List<ResumeInfo.WorkExperience> workExperiences;

    private List<ResumeInfo.Project> projects;
}
