package com.shea.agent.interviewagent.config;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.checkpoint.BaseCheckpointSaver;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.shea.agent.interviewagent.utils.NodeBeanUtil;
import com.shea.agent.interviewagent.workflow.dispathcer.HumanFeedbackDispatcher;
import com.shea.agent.interviewagent.workflow.dispathcer.InputDispatcher;
import com.shea.agent.interviewagent.workflow.dispathcer.PlanExecuteDispatcher;
import com.shea.agent.interviewagent.workflow.node.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
                .addNode(ANSWER_WITH_RAG_NODE,nodeBeanUtil.getAsyncNodeBean(AnswerWithRagNode.class))
                .addNode(EVALUATE_USER_QUERY_NODE,nodeBeanUtil.getAsyncNodeBean(EvaluateUserAnswerNode.class))
                .addNode(PLANNER_NODE,nodeBeanUtil.getAsyncNodeBean(PlannerNode.class))
                .addNode(SUMMARIZE_INTERVIEW_NODE,nodeBeanUtil.getAsyncNodeBean(SummarizeInterviewNode.class))
                .addNode(HUMAN_FEEDBACK_NODE,nodeBeanUtil.getAsyncNodeBean(HumanFeedbackNode.class));

        stateGraph
                .addConditionalEdges(StateGraph.START, AsyncEdgeAction.edge_async(new InputDispatcher()),
                Map.of(ENHANCE_USER_QUERY_NODE,ENHANCE_USER_QUERY_NODE,
                        PARSE_RESUME_INFO_NODE,PARSE_RESUME_INFO_NODE,
                        EVALUATE_USER_QUERY_NODE,EVALUATE_USER_QUERY_NODE,
                        END,END))
                .addConditionalEdges(PLANNER_NODE,AsyncEdgeAction.edge_async(new PlanExecuteDispatcher()),
                        Map.of(GENERATE_QUESTION_NODE,GENERATE_QUESTION_NODE,
                                SUMMARIZE_INTERVIEW_NODE,SUMMARIZE_INTERVIEW_NODE))
                .addConditionalEdges(HUMAN_FEEDBACK_NODE,AsyncEdgeAction.edge_async(new HumanFeedbackDispatcher()),
                        Map.of(HUMAN_FEEDBACK_NODE, HUMAN_FEEDBACK_NODE,
                                GENERATE_QUESTION_NODE, GENERATE_QUESTION_NODE,
                                END, END))
                .addEdge(PARSE_RESUME_INFO_NODE, HUMAN_FEEDBACK_NODE)
                .addEdge(GENERATE_QUESTION_NODE, END)
                .addEdge(EVALUATE_USER_QUERY_NODE, PLANNER_NODE)
                .addEdge(ENHANCE_USER_QUERY_NODE,ANSWER_WITH_RAG_NODE)
                .addEdge(ANSWER_WITH_RAG_NODE,END)
                .addEdge(SUMMARIZE_INTERVIEW_NODE,END);
        return stateGraph;
    }

    @NotNull
    private static StateGraph getStateGraph() {
        KeyStrategyFactory keyStrategyFactory = () -> {
            Map<String, KeyStrategy> strategies = new HashMap<>();
            strategies.put(INPUT_FILE,KeyStrategy.REPLACE);
            strategies.put(OUTPUT_INFO,KeyStrategy.REPLACE);
            strategies.put(CHAT_ID,KeyStrategy.REPLACE);
            strategies.put(QUESTION,KeyStrategy.REPLACE);
            strategies.put(FLUX_ID,KeyStrategy.REPLACE);
            strategies.put(ANSWER_WITH_RAG,KeyStrategy.REPLACE);
            strategies.put(MULTI_TURN,KeyStrategy.REPLACE);
            strategies.put(ENHANCED_QUERY,KeyStrategy.REPLACE);
            strategies.put(USER_REPLY_ANSWER,KeyStrategy.REPLACE);
            strategies.put(EVALUATIONS,KeyStrategy.REPLACE);
            strategies.put(NEXT_STEP,KeyStrategy.REPLACE);
            strategies.put(CURRENT_PHASE,KeyStrategy.REPLACE);
            strategies.put(INPUT_KEY,KeyStrategy.REPLACE);
            strategies.put(HUMAN_FEEDBACK_DATA,KeyStrategy.REPLACE);
            strategies.put(RESUME_SAVED,KeyStrategy.REPLACE);
            return strategies;
        };

        return new StateGraph(INTERVIEW_AGENT_NAME,keyStrategyFactory);
    }

    @Bean
    public CompileConfig compileConfig(BaseCheckpointSaver checkpointSaver) {
        SaverConfig saverConfig = SaverConfig.builder().register(checkpointSaver).build();
        return CompileConfig.builder().saverConfig(saverConfig).interruptBefore(HUMAN_FEEDBACK_NODE).build();
    }

    @Bean
    @ConditionalOnProperty(name = "interview-agent.checkpoint.type", havingValue = "memory")
    public BaseCheckpointSaver memoryCheckpointSaver() {
        return MemorySaver.builder().build();
    }
}
