package com.shea.agent.interviewagent.workflow.dispathcer;

import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import com.shea.agent.interviewagent.utils.StateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.shea.agent.interviewagent.constant.Constant.*;

/**
 * @author : Shea.
 * @since : 2026/7/22 14:52
 */
@Component
@Slf4j
public class InputDispatcher implements EdgeAction {
    @Override
    public String apply(OverAllState state) throws Exception {
        String filePath = StateUtil.getStringValue(state,INPUT_FILE,"");
        String userQuery = StateUtil.getStringValue(state,INPUT_KEY,"");
        String userReplyAnswer = StateUtil.getStringValue(state, USER_REPLY_ANSWER, "");
        String currentPhase = StateUtil.getStringValue(state, CURRENT_PHASE, "general_chat");
        if (StrUtil.isNotBlank(filePath)) {
            return PARSE_RESUME_INFO_NODE;
        }
        if (INTERVIEW_PHASE.equals(currentPhase)) {
            if (StrUtil.isNotBlank(userReplyAnswer)) {
                return EVALUATE_USER_QUERY_NODE;
            }

            if (StrUtil.isNotBlank(userQuery)) {
                return ENHANCE_USER_QUERY_NODE;
            }
        }
        if (StrUtil.isNotBlank(userQuery)) {
            return ENHANCE_USER_QUERY_NODE;
        }
        return END;
    }
}
