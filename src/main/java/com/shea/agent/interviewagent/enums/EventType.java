package com.shea.agent.interviewagent.enums;

import lombok.Getter;

/**
 * 事件类型枚举
 * @author : Shea.
 * @since : 2026/8/3 14:12
 */
@Getter
public enum EventType {

    TOKEN("token"),
    THINKING("thinking"),
    TOOL_CALL("tool_call"),
    DONE("done"),
    ERROR("error");

    private final String value;

    EventType(String value) {
        this.value = value;
    }
}
