package com.shea.agent.interviewagent.config;

import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.shea.agent.interviewagent.utils.NodeBeanUtil;
import com.shea.agent.interviewagent.workflow.node.JobDescriptionSummaryNode;
import com.shea.agent.interviewagent.workflow.node.JobResumeMatchNode;
import com.shea.agent.interviewagent.workflow.node.ParseResumeInfoNode;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.shea.agent.interviewagent.constant.Constant.*;

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
        stateGraph.addNode(JOB_DESCRIPTION_SUMMARY_NODE,
                nodeBeanUtil.getAsyncNodeBean(JobDescriptionSummaryNode.class))
                .addNode(PARSE_RESUME_INFO_NODE,nodeBeanUtil.getAsyncNodeBean(ParseResumeInfoNode.class))
                .addNode(JOB_RESUME_MATCH_NODE,nodeBeanUtil.getAsyncNodeBean(JobResumeMatchNode.class));
        stateGraph.addEdge(StateGraph.START, JOB_DESCRIPTION_SUMMARY_NODE)
                .addEdge(StateGraph.START, PARSE_RESUME_INFO_NODE)
                .addEdge(JOB_DESCRIPTION_SUMMARY_NODE,JOB_RESUME_MATCH_NODE)
                .addEdge(PARSE_RESUME_INFO_NODE,JOB_RESUME_MATCH_NODE)
                .addEdge(JOB_RESUME_MATCH_NODE,StateGraph.END);
        return stateGraph;
    }
}
