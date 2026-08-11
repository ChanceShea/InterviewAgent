package com.shea.agent.interviewagent.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author : Shea.
 * @since : 2026/8/10 14:17
 */
@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String token;

    private Long id;

    private String uuid;

    private String username;

    private String phone;

    private String email;

    private String nickname;

    private String avatarUrl;

    private LocalDateTime createTime;

    private LocalDateTime lastLoginAt;
}
