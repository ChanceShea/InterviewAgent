package com.shea.agent.interviewagent.config;

import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.shea.agent.interviewagent.utils.NodeBeanUtil;
import com.shea.agent.interviewagent.workflow.node.JobDescriptionSummaryNode;
import com.shea.agent.interviewagent.workflow.node.JobResumeMatchNode;
import com.shea.agent.interviewagent.workflow.node.ParallelNodeWrapper;
import com.shea.agent.interviewagent.workflow.node.ParseResumeInfoNode;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.shea.agent.interviewagent.constant.Constant.JOB_RESUME_MATCH_NODE;

/**
 * @author : Shea.
 * @since : 2026/7/30 20:32
 */
@Configuration
public class JobResumeMatchAgentConfig {

    @Resource
    private NodeBeanUtil nodeBeanUtil;

    @Bean
    public StateGraph jobResumeMatchGraph() throws GraphStateException {
        StateGraph stateGraph = new StateGraph();

        // 创建并行节点，同时执行 JD 总结和简历解析
        String PARALLEL_START_NODE = "parallelStartNode";
        stateGraph.addNode(PARALLEL_START_NODE,
                new ParallelNodeWrapper(List.of(
                        nodeBeanUtil.getAsyncNodeBean(JobDescriptionSummaryNode.class),
                        nodeBeanUtil.getAsyncNodeBean(ParseResumeInfoNode.class)
                )));

        stateGraph.addNode(JOB_RESUME_MATCH_NODE,
                nodeBeanUtil.getAsyncNodeBean(JobResumeMatchNode.class));

        // START -> 并行节点 -> 人工反馈 -> 匹配 -> END
        stateGraph.addEdge(StateGraph.START, PARALLEL_START_NODE)
                .addEdge(PARALLEL_START_NODE, JOB_RESUME_MATCH_NODE)
                .addEdge(JOB_RESUME_MATCH_NODE, END);
        return stateGraph;
    }
}
