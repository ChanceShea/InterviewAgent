package com.shea.agent.interviewagent.workflow.node;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static com.shea.agent.interviewagent.constant.Constant.CHAT_ID;
import static com.shea.agent.interviewagent.constant.Constant.INPUT_FILE;

@SpringBootTest
class ParseResumeInfoNodeTest {

    @Resource
    private ParseResumeInfoNode parseResumeInfoNode;

    @Test
    void apply() throws Exception {
        OverAllState state = new OverAllState();
        state.registerKeyAndStrategy(CHAT_ID,(oldV,newV) -> newV);
        state.registerKeyAndStrategy(INPUT_FILE,(oldV,newV) -> newV);
        Map<String,Object> map = new HashMap<>();
        map.put(CHAT_ID, RandomUtil.randomString(16));
        map.put(INPUT_FILE,"tmp/resume/info.pdf");
        state.input(map);
        Map<String, Object> apply = parseResumeInfoNode.apply(state);
        System.out.println(apply);
    }
}