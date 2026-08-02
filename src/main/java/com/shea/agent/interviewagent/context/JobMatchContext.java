package com.shea.agent.interviewagent.context;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : Shea.
 * @since : 2026/7/31 09:03
 */
@Component
public class JobMatchContext {

    private final Map<String,String> contextMap = new ConcurrentHashMap<>();

    public void add(String key, String value) {
        contextMap.put(key, value);
    }

    public String get(String key) {
        return contextMap.get(key);
    }

    public Map<String,String> getContextMap() {
        return contextMap;
    }
}
