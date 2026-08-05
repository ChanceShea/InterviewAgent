package com.shea.agent.interviewagent.workflow.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 并行节点包装器，用于同时执行多个节点
 * @author : Shea.
 * @since : 2026/8/5
 */
@Slf4j
@Component
public class ParallelNodeWrapper implements AsyncNodeAction {

    private final List<AsyncNodeAction> parallelNodes;

    public ParallelNodeWrapper(List<AsyncNodeAction> parallelNodes) {
        this.parallelNodes = parallelNodes;
    }

    @Override
    public CompletableFuture<Map<String, Object>> apply(OverAllState state) {
        log.info("并行节点开始执行，包含 {} 个子节点", parallelNodes.size());
        // 并行执行所有节点
        List<CompletableFuture<Map<String, Object>>> futures = parallelNodes.stream()
                .map(node -> node.apply(state).whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("并行子节点执行失败", ex);
                    } else {
                        log.info("并行子节点执行完成，结果: {}", result);
                    }
                }))
                .toList();

        // 等待所有节点完成并合并结果
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    Map<String, Object> mergedResult = new ConcurrentHashMap<>();
                    for (CompletableFuture<Map<String, Object>> future : futures) {
                        Map<String, Object> result = future.join();
                        if (result != null) {
                            mergedResult.putAll(result);
                        }
                    }
                    log.info("并行节点执行完成，合并结果: {}", mergedResult);
                    return mergedResult;
                });
    }
}
