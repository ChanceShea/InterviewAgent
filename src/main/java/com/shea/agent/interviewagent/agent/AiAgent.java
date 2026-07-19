package com.shea.agent.interviewagent.agent;

import com.shea.agent.interviewagent.advisor.MyLoggerAdvisor;
import lombok.Getter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * @author : Shea.
 * @since : 2026/7/19 16:53
 */
@Component
@Getter
public class AiAgent {

    private final ChatClient chatClient;

    public AiAgent(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
    }
}
