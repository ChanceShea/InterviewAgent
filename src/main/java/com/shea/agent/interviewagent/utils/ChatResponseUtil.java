package com.shea.agent.interviewagent.utils;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import java.util.List;

/**
 * @author : Shea.
 * @since : 2026/7/20 16:35
 */
public class ChatResponseUtil {

    public static ChatResponse createResponse(String statusMessage) {
        return createPureResponse(statusMessage + "\n");
    }

    public static ChatResponse createPureResponse(String message) {
        AssistantMessage assistantMessage = new AssistantMessage(message);
        Generation generation = new Generation(assistantMessage);
        return new ChatResponse(List.of(generation));
    }


    public static String getText(ChatResponse chatResponse) {
        Generation result = chatResponse.getResult();
        if (result == null) {
            return "";
        }
        AssistantMessage output = result.getOutput();
        if (output == null) {
            return "";
        }
        return output.getText() == null ? "" : output.getText();
    }
}
