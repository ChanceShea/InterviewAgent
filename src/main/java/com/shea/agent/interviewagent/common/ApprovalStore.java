package com.shea.agent.interviewagent.common;

import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 批准存储，用于存储用户的批准信息
 * @author : Shea.
 * @since : 2026/7/2 18:12
 */
@Component
public class ApprovalStore {

    private final Map<String, InterruptionMetadata> map = new ConcurrentHashMap<>();

    public void save(String chatId,InterruptionMetadata metadata) {
        map.put(chatId, metadata);
    }

    public InterruptionMetadata get(String chatId) {
        return map.get(chatId);
    }

    public void remove(String chatId) {
        map.remove(chatId);
    }
}
