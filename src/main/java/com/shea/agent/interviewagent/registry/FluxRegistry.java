package com.shea.agent.interviewagent.registry;

import com.alibaba.cloud.ai.graph.GraphResponse;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : Shea.
 * @since : 2026/7/21 10:42
 */
@Component
public class FluxRegistry {

    private static final Map<String, Flux<GraphResponse<StreamingOutput>>> fluxRegistry = new ConcurrentHashMap<>();

    public void addFlux(String fluxId,Flux<GraphResponse<StreamingOutput>> flux) {
        fluxRegistry.putIfAbsent(fluxId,flux);
    }

    public Flux<GraphResponse<StreamingOutput>> get(String id) {
        return fluxRegistry.remove(id);  // 使用后移除
    }
}
