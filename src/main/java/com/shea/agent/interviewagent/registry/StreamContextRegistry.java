package com.shea.agent.interviewagent.registry;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.shea.agent.interviewagent.context.StreamContext;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 管理各 chatId 对应的 StreamContext，供 AiServiceImpl 和节点共享访问
 */
@Component
public class StreamContextRegistry {

    private final Cache<String, StreamContext> contextMap = Caffeine.newBuilder()
            .initialCapacity(1024)
            .maximumSize(10_000L) // 最大缓存数量
            .expireAfterAccess(Duration.ofMinutes(30)) // 缓存过期时间
            .removalListener((k,ctx,cause) -> {
                if (ctx != null) {
                    ((StreamContext)ctx).cleanup();
                }
            })
            .build();

    public StreamContext getOrCreate(String chatId) {
        return contextMap.asMap().computeIfAbsent(chatId, k -> new StreamContext());
    }

    public StreamContext get(String chatId) {
        return contextMap.getIfPresent(chatId);
    }

    public void remove(String chatId) {
        contextMap.invalidate(chatId);
    }
}
