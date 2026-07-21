package com.shea.agent.interviewagent.workflow.node;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.cloud.ai.graph.GraphResponse;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.shea.agent.interviewagent.registry.FluxRegistry;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.shea.agent.interviewagent.constant.Constant.*;

@SpringBootTest
class GenerateQuestionNodeTest {

    @Resource
    private ParseResumeInfoNode parseResumeInfoNode;
    @Resource
    private GenerateQuestionNode generateQuestionNode;
    @Resource
    private FluxRegistry registry;

    @Test
    void apply() throws Exception {
        // 1. 先解析简历，拿到 ResumeInfo
        OverAllState state = new OverAllState();
        state.registerKeyAndStrategy(CHAT_ID, (oldV, newV) -> newV);
        state.registerKeyAndStrategy(INPUT_FILE, (oldV, newV) -> newV);
        state.registerKeyAndStrategy(OUTPUT_INFO, (oldV, newV) -> newV);
        state.registerKeyAndStrategy(QUESTIONS, (oldV, newV) -> newV);
        state.registerKeyAndStrategy(FLUX_ID, (oldV, newV) -> newV);

        Map<String, Object> input = new HashMap<>();
        String chatId = RandomUtil.randomString(16);
        input.put(CHAT_ID, chatId);
        input.put(INPUT_FILE, "tmp/resume/info.pdf");
        state.input(input);

        Map<String, Object> resumeResult = parseResumeInfoNode.apply(state);
        state.input(resumeResult);
        System.out.println("简历解析完成：" + resumeResult);

        // 2. 生成面试问题
        Map<String, Object> questionResult = generateQuestionNode.apply(state);
        System.out.println("节点返回：" + questionResult);

        // 3. 订阅 generator，查看流式输出
        String fluxId = questionResult.get(FLUX_ID).toString();
        Flux<GraphResponse<StreamingOutput>> generator = registry.get(fluxId);
        if (generator instanceof Flux) {
            Flux<GraphResponse<StreamingOutput>> flux = (Flux<GraphResponse<StreamingOutput>>) generator;
            flux.doOnNext(resp -> {
                if (resp.getOutput() == null) {
                    System.out.println("[" + java.time.LocalTime.now() + "] [终止信号]");
                    return;
                }
                try {
                    Object data = resp.getOutput().get();
                    if (data instanceof StreamingOutput output) {
                        System.out.println("[" + java.time.LocalTime.now() + "] " + output.getOriginData());
                    }
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }).doOnComplete(() -> {
                System.out.println("\n\n流式输出完成");
            }).doOnError(e -> {
                System.err.println("流式输出出错：" + e.getMessage());
            }).blockLast();
        }
    }
}
