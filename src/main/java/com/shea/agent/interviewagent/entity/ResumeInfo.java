package com.shea.agent.interviewagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 
 * </p>
 *
 * @author Shea
 * @since 2026-08-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "resume_info", autoResultMap = true)
@NoArgsConstructor
@AllArgsConstructor
public class ResumeInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 求职者姓名
     */
    private String name;

    /**
     * 岗位名称
     */
    private String job;

    /**
     * 技能
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> skills;

    /**
     * 工作经历
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<WorkExperience> workExperience;

    /**
     * 项目经历
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Project> project;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDate createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDate updateTime;

    /**
     * 删除字段（0：未删除，1：删除）
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer isDeleted;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkExperience {
        private String companyName;
        private String responsibilities;
        private String duration;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Project {
        private String projectName;
        private String duration;
        private List<String> techStack;
        private String description;
    }
}
