package com.shea.agent.interviewagent.registry;

import com.shea.agent.interviewagent.context.StreamContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 管理各 chatId 对应的 StreamContext，供 AiServiceImpl 和节点共享访问
 */
@Component
public class StreamContextRegistry {

    private final ConcurrentMap<String, StreamContext> contextMap = new ConcurrentHashMap<>();

    public StreamContext getOrCreate(String chatId) {
        return contextMap.computeIfAbsent(chatId, k -> new StreamContext());
    }

    public StreamContext get(String chatId) {
        return contextMap.get(chatId);
    }

    public void remove(String chatId) {
        contextMap.remove(chatId);
    }
}
