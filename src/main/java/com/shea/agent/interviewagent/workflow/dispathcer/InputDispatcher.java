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
        boolean hasQuery = StrUtil.isBlank(userQuery);
        boolean hasFile = StrUtil.isBlank(filePath);
        if (hasFile && hasQuery) {
            log.info("用户未上传简历文件，且没有提问，结束");
            return END;
        }
        if (!hasFile) {
            return PARSE_RESUME_INFO_NODE;
        }
        return ENHANCE_USER_QUERY_NODE;
    }
}
