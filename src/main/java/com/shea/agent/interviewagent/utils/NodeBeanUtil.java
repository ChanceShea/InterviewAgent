package com.shea.agent.interviewagent.utils;

import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @author : Shea.
 * @since : 2026/7/20 17:53
 */
@Component
@AllArgsConstructor
public class NodeBeanUtil {

    private final ApplicationContext context;

    public <T extends NodeAction> NodeAction getNodeBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    public <T extends NodeAction> AsyncNodeAction getAsyncNodeBean(Class<T> clazz) {
        return AsyncNodeAction.node_async(getNodeBean(clazz));
    }

    public <T extends EdgeAction> EdgeAction getEdgeBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    public <T extends EdgeAction> AsyncEdgeAction getAsyncEdgeBean(Class<T> clazz) {
        return AsyncEdgeAction.edge_async(getEdgeBean(clazz));
    }
}
