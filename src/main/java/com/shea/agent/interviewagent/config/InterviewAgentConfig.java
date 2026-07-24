package com.shea.agent.interviewagent.config;

import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.shea.agent.interviewagent.utils.NodeBeanUtil;
import com.shea.agent.interviewagent.workflow.dispathcer.InputDispatcher;
import com.shea.agent.interviewagent.workflow.node.AnswerWithRagNode;
import com.shea.agent.interviewagent.workflow.node.EnhanceUserQueryNode;
import com.shea.agent.interviewagent.workflow.node.GenerateQuestionNode;
import com.shea.agent.interviewagent.workflow.node.ParseResumeInfoNode;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
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
        stateGraph
                .addNode(PARSE_RESUME_INFO_NODE,nodeBeanUtil.getAsyncNodeBean(ParseResumeInfoNode.class))
                .addNode(GENERATE_QUESTION_NODE,nodeBeanUtil.getAsyncNodeBean(GenerateQuestionNode.class))
                .addNode(ENHANCE_USER_QUERY_NODE,nodeBeanUtil.getAsyncNodeBean(EnhanceUserQueryNode.class))
                .addNode(ANSWER_WITH_RAG_NODE,nodeBeanUtil.getAsyncNodeBean(AnswerWithRagNode.class));

        stateGraph
                .addConditionalEdges(StateGraph.START, AsyncEdgeAction.edge_async(new InputDispatcher()),
                Map.of(ENHANCE_USER_QUERY_NODE,ENHANCE_USER_QUERY_NODE,
                        PARSE_RESUME_INFO_NODE,PARSE_RESUME_INFO_NODE,
                        END,END))
                .addEdge(PARSE_RESUME_INFO_NODE, GENERATE_QUESTION_NODE)
                .addEdge(ENHANCE_USER_QUERY_NODE,ANSWER_WITH_RAG_NODE)
                .addEdge(GENERATE_QUESTION_NODE, END)
                .addEdge(ANSWER_WITH_RAG_NODE,END);
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
            strategies.put(ANSWER_WITH_RAG,KeyStrategy.REPLACE);
            return strategies;
        };

        return new StateGraph(INTERVIEW_AGENT_NAME,keyStrategyFactory);
    }
}
