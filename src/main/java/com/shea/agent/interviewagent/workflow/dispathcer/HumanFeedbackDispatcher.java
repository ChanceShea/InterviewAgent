package com.shea.agent.interviewagent.workflow.dispathcer;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import com.shea.agent.interviewagent.utils.StateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.shea.agent.interviewagent.constant.Constant.*;

/**
 * @author : Shea.
 * @since : 2026/8/5 10:48
 */
@Slf4j
@Component
public class HumanFeedbackDispatcher implements EdgeAction {
    @Override
    public String apply(OverAllState state) throws Exception {
        String nextStep = StateUtil.getStringValue(state, NEXT_STEP, END);
        log.info("HumanFeedbackDispatcher: nextStep={}", nextStep);
        if (WAIT_FOR_FEEDBACK.equals(nextStep)) {
            log.info("返回 HUMAN_FEEDBACK_NODE");
            return HUMAN_FEEDBACK_NODE;
        }
        if (GENERATE_QUESTION_NODE.equals(nextStep)) {
            log.info("返回 GENERATE_QUESTION_NODE");
            return GENERATE_QUESTION_NODE;
        }
        log.info("返回 END");
        return END;
    }
}
