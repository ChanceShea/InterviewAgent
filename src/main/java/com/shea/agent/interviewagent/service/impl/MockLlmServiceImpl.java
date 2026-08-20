package com.shea.agent.interviewagent.service.impl;

import com.shea.agent.interviewagent.service.LlmService;
import com.shea.agent.interviewagent.utils.ChatResponseUtil;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Service
@Profile("mock")
@Primary
public class MockLlmServiceImpl implements LlmService {

    // 对话路径(AnswerWithRag)期望:AnswerUserQueryDTO
    private static final String ANSWER_JSON =
            "{\"answer\":\"这是mock生成的测试回答，用于压测流式输出的token速率和背压表现。\","
                    + "\"citations\":[\"doc1\",\"doc2\"],\"confidence\":0.85,"
                    + "\"standalone_query\":\"这是mock重写后的独立查询语句，用于压测向量检索。\","
                    + "\"standaloneQuery\":\"这是mock重写后的独立查询语句，用于压测向量检索。\","
                    + "\"summary\":\"这是mock生成的面试总结。\","
                    + "\"score\":5,\"next_step\":\"generate_question\","
                    + "\"nextStep\":\"generateQuestionNode\"}";

    // 简历解析路径(ParseResumeInfoNode)期望:ResumeInfoDTO
    private static final String RESUME_JSON =
            "{\"name\":\"张三\",\"job\":\"Java后端工程师\","
                    + "\"skills\":[\"Java\",\"Spring Boot\",\"MySQL\",\"Redis\"],"
                    + "\"workExperiences\":[{\"companyName\":\"某科技有限公司\",\"responsibilities\":\"负责后端服务设计与开发\",\"duration\":\"2021.06-2024.06\"}],"
                    + "\"projects\":[{\"projectName\":\"电商平台\",\"duration\":\"2022.03-2023.12\",\"techStack\":[\"Spring Cloud\",\"MySQL\",\"Redis\"],\"description\":\"高并发电商系统\"}]}";

    /** 把完整字符串切成小段,模拟 LLM 流式输出 */
    private Flux<ChatResponse> chunkStream(String full, int chunkSize, Duration interval) {
        int total = (full.length() + chunkSize - 1) / chunkSize;
        return Flux.interval(interval)
                .take(total)
                .map(i -> {
                    int s = (int) (i * chunkSize);
                    int e = Math.min(full.length(), s + chunkSize);
                    return ChatResponseUtil.createPureResponse(full.substring(s, e));
                });
    }

    // 简历解析节点用 call():返回 ResumeInfoDTO,间隔稍大模拟解析耗时
    @Override
    public Flux<ChatResponse> call(String system, String user) {
        return chunkStream(RESUME_JSON, 4, Duration.ofMillis(40));
    }

    @Override
    public Flux<ChatResponse> call(String system, String user, Class<?> t) {
        return chunkStream(RESUME_JSON, 4, Duration.ofMillis(40));
    }

    // 对话/总结节点用 callUser():返回 AnswerUserQueryDTO
    @Override
    public Flux<ChatResponse> callUser(String user) {
        return chunkStream(ANSWER_JSON, 6, Duration.ofMillis(50));
    }

    @Override
    public Flux<ChatResponse> callUser(String user, Class<?> t) {
        return chunkStream(ANSWER_JSON, 6, Duration.ofMillis(50));
    }
}
