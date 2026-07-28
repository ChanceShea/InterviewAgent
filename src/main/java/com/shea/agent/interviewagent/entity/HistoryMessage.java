package com.shea.agent.interviewagent.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : Shea.
 * @since : 2026/7/28 10:42
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryMessage {

    private String chatId;

    // ASSISTANT/USER/SYSTEM
    private String messageType;

    private String message;
}
