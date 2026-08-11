package com.shea.agent.interviewagent.enums;

import lombok.Getter;

/**
 * @author : Shea.
 * @since : 2026/8/10 14:23
 */
@Getter
public enum UserStatusEnum {

    DISABLED(0,"禁用"),
    ENABLED(1,"正常"),
    FROZEN(2,"冻结");

    private final Integer value;
    private final String text;


    UserStatusEnum(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    public static UserStatusEnum getUserStatusEnum(Integer value) {
        if (value == null) {
            return null;
        }
        for (UserStatusEnum userStatusEnum : UserStatusEnum.values()) {
            if (userStatusEnum.value.equals(value)) {
                return userStatusEnum;
            }
        }
        return null;
    }
}
