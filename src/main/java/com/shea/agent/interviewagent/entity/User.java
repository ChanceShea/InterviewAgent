package com.shea.agent.interviewagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户主表，存储用户基本信息和登录凭证
 * </p>
 *
 * @author Shea
 * @since 2026-08-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("\"user\"")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID，自增主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户唯一标识（对外暴露，不暴露自增ID）
     */
    private String uuid;

    /**
     * 用户名（可选，支持手机号/邮箱登录，唯一索引）
     */
    private String username;

    /**
     * 手机号（唯一索引）
     */
    private String phone;

    /**
     * 邮箱（唯一索引）
     */
    private String email;

    /**
     * 密码（BCrypt加密存储）
     */
    private String password;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像URL
     */
    private String avatarUrl;

    /**
     * 用户状态：0-禁用 1-正常 2-冻结
     */
    private Integer status;

    /**
     * 逻辑删除标识：0-未删除 1-已删除
     */
    private Integer isDeleted;

    /**
     * 注册时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间（自动更新）
     */
    private LocalDateTime updatedAt;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;


}
