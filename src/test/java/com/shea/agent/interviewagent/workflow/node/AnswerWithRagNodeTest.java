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
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.shea.agent.interviewagent.constant.Constant.*;

@SpringBootTest
class AnswerWithRagNodeTest {

    @Resource
    private AnswerWithRagNode answerWithRagNode;
    @Resource
    private FluxRegistry fluxRegistry;

    @Test
    void apply() throws Exception {

        OverAllState state = new OverAllState();
        state.registerKeyAndStrategy(ENHANCED_QUERY,new ReplaceStrategy());
        state.registerKeyAndStrategy(MULTI_TURN_QUERY,new ReplaceStrategy());
        state.registerKeyAndStrategy(CHAT_ID,new ReplaceStrategy());
        state.registerKeyAndStrategy(FLUX_ID,new ReplaceStrategy());
        Map<String,Object> params = new HashMap<>();
        params.put(ENHANCED_QUERY,"线程池有哪些参数");
        params.put(CHAT_ID,"11111");
        state.updateState(params);

        Map<String, Object> apply = answerWithRagNode.apply(state);
        state.updateState(apply);
        String fluxId = StateUtil.getStringValue(state, FLUX_ID);
        Flux<GraphResponse<StreamingOutput>> flux = fluxRegistry.get(fluxId);
        if (flux != null) {
            flux.publishOn(Schedulers.boundedElastic()).flatMap(resp -> {
                if (resp.getOutput() == null) {
                    return Flux.empty();
                }
                StreamingOutput streamingOutput = null;
                try {
                    streamingOutput = resp.getOutput().get();
                    String res = handleResultStr(streamingOutput);
                    System.out.println(res);
                    return Flux.just(res);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).blockLast();
        }
    }

    private static String handleResultStr(StreamingOutput streamingOutput) {
        String resultStr = streamingOutput.getOriginData().toString();
        Pattern pattern = Pattern.compile("textContent=(.*?), metadata=", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(resultStr);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return resultStr;
    }
}