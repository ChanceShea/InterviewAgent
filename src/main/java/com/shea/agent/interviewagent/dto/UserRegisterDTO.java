package com.shea.agent.interviewagent.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author : Shea.
 * @since : 2026/8/10 14:31
 */
@Data
public class UserRegisterDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private String confirmPassword;
}
