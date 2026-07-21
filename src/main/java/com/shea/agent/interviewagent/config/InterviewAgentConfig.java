package com.shea.agent.interviewagent.config;

import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.shea.agent.interviewagent.utils.NodeBeanUtil;
import com.shea.agent.interviewagent.workflow.node.GenerateQuestionNode;
import com.shea.agent.interviewagent.workflow.node.ParseResumeInfoNode;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.shea.agent.interviewagent.constant.Constant.*;

/**
 * @author : Shea.
 * @since : 2026/7/20 17:44
 */
@Configuration
public class InterviewAgentConfig {

    @Bean
    public StateGraph interviewGraph(NodeBeanUtil nodeBeanUtil) throws GraphStateException {
        StateGraph stateGraph = getStateGraph();
        stateGraph.addNode(PARSE_RESUME_INFO_NODE,nodeBeanUtil.getAsyncNodeBean(ParseResumeInfoNode.class))
                .addNode(GENERATE_QUESTION_NODE,nodeBeanUtil.getAsyncNodeBean(GenerateQuestionNode.class));

        stateGraph.addEdge(StateGraph.START, PARSE_RESUME_INFO_NODE)
                .addEdge(PARSE_RESUME_INFO_NODE, GENERATE_QUESTION_NODE)
                .addEdge(GENERATE_QUESTION_NODE, StateGraph.END);
        return stateGraph;
    }

    @NotNull
    private static StateGraph getStateGraph() {
        KeyStrategyFactory keyStrategyFactory = () -> {
            Map<String, KeyStrategy> strategies = new HashMap<>();
            strategies.put(INPUT_FILE,KeyStrategy.REPLACE);
            strategies.put(OUTPUT_INFO,KeyStrategy.REPLACE);
            strategies.put(CHAT_ID,KeyStrategy.REPLACE);
            strategies.put(QUESTIONS,KeyStrategy.REPLACE);
            strategies.put(FLUX_ID,KeyStrategy.REPLACE);
            return strategies;
        };

        return new StateGraph(INTERVIEW_AGENT_NAME,keyStrategyFactory);
    }
}
