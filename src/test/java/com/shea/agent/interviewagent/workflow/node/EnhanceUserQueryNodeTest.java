package com.shea.agent.interviewagent.workflow.node;

import com.alibaba.cloud.ai.graph.GraphResponse;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.shea.agent.interviewagent.registry.FluxRegistry;
import com.shea.agent.interviewagent.utils.StateUtil;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.Map;

import static com.shea.agent.interviewagent.constant.Constant.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class EnhanceUserQueryNodeTest {

    @Resource
    private EnhanceUserQueryNode enhanceUserQueryNode;
    @Resource
    private FluxRegistry registry;

    OverAllState createTestState() {
        OverAllState state = new OverAllState();
        state.registerKeyAndStrategy(INPUT_KEY,new ReplaceStrategy());
        state.registerKeyAndStrategy(CHAT_ID,new ReplaceStrategy());
        state.registerKeyAndStrategy(ENHANCED_QUERY,new ReplaceStrategy());
        state.registerKeyAndStrategy(FLUX_ID,new ReplaceStrategy());
        state.registerKeyAndStrategy(MULTI_TURN_QUERY,new ReplaceStrategy());
        return state;
    }

    @Test
    void apply() throws Exception {
        OverAllState state = createTestState();
        Map<String, Object> inputKey = Map.of(
                INPUT_KEY, "那它适用于什么场景",
                MULTI_TURN_QUERY, "RedLock是什么",
                CHAT_ID, "11111"
        );
        state.updateState(inputKey);
        Map<String, Object> apply = enhanceUserQueryNode.apply(state);
        assertNotNull(apply);
        state.updateState(apply);
        String fluxId = StateUtil.getStringValue(state, FLUX_ID);
        Flux<GraphResponse<StreamingOutput>> flux = registry.get(fluxId);
        flux.doOnNext(output -> {
            if (output.getOutput() == null) {
                System.out.println("终止信号");
                return;
            }
            StreamingOutput streamingOutput;
            try {
                streamingOutput = output.getOutput().get();
                String resultStr = streamingOutput.getOriginData().toString();
                System.out.println(resultStr);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).doOnComplete(() -> {
            System.out.println("流式输出完成\n\n");
        }).doOnError(e -> {
            System.out.println("流式输出错误：" + e.getMessage());
        }).blockLast();
    }
}