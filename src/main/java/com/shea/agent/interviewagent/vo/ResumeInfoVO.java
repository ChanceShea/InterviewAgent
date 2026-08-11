package com.shea.agent.interviewagent.vo;

import com.shea.agent.interviewagent.entity.ResumeInfo;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author : Shea.
 * @since : 2026/8/10 14:07
 */
@Data
public class ResumeInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private String name;

    private String job;

    private List<String> skills;

    private List<ResumeInfo.Project> project;

    private List<ResumeInfo.WorkExperience> experience;

    private LocalDateTime createTime;
}
