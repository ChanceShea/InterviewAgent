package com.shea.agent.interviewagent.workflow.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.shea.agent.interviewagent.dto.ResumeInfoDTO;
import com.shea.agent.interviewagent.service.IResumeInfoService;
import com.shea.agent.interviewagent.utils.StateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.shea.agent.interviewagent.constant.Constant.*;

/**
 * @author : Shea.
 * @since : 2026/8/5 10:29
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class HumanFeedbackNode implements NodeAction {

    private final IResumeInfoService resumeInfoService;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        log.info("HumanFeedbackNode 开始执行");
        Map<String,Object> feedback = StateUtil.getObjectValue(state, HUMAN_FEEDBACK_DATA, Map.class, Map.of());
        log.info("反馈数据: {}", feedback);
        if (feedback.isEmpty()) {
            log.info("没有反馈数据，等待用户反馈");
            return Map.of(NEXT_STEP,WAIT_FOR_FEEDBACK);
        }
        boolean approved = Boolean.parseBoolean(String.valueOf(feedback.getOrDefault("approved", false)));
        boolean saved = false;
        if (approved) {
            ResumeInfoDTO info = StateUtil.getObjectValue(state, OUTPUT_INFO, ResumeInfoDTO.class);
            Long userId = StateUtil.getLongValue(state, USER_ID);
            saved = resumeInfoService.saveResume(info,userId);
            log.info("用户同意持久化简历，保存结果：{}",saved);
        } else {
            log.info("用户拒绝持久化简历");
        }
        return Map.of(NEXT_STEP,GENERATE_QUESTION_NODE,RESUME_SAVED,saved);
    }
}
