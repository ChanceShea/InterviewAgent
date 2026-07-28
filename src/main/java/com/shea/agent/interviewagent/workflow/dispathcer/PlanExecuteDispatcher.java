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
 * @since : 2026/7/28 10:17
 */
@Component
@Slf4j
public class PlanExecuteDispatcher implements EdgeAction {
    @Override
    public String apply(OverAllState state) throws Exception {
        String phase = StateUtil.getStringValue(state, NEXT_STEP, "");
        log.info("PlanExecuteDispatcher 读取到 NEXT_STEP: '{}'", phase);
        if (GENERATE_QUESTION_NODE.equals(phase)) {
            log.info("PlanExecuteDispatcher 决策: 跳转到 GENERATE_QUESTION_NODE");
            return GENERATE_QUESTION_NODE;
        }
        log.info("PlanExecuteDispatcher 决策: 跳转到 END (phase={})", phase);
        return END;
    }
}
