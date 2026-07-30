package com.shea.agent.interviewagent.workflow.node;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.graph.GraphResponse;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.shea.agent.interviewagent.dto.AnswerEvaluation;
import com.shea.agent.interviewagent.entity.Project;
import com.shea.agent.interviewagent.entity.ResumeInfo;
import com.shea.agent.interviewagent.entity.WorkExperience;
import com.shea.agent.interviewagent.registry.FluxRegistry;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.shea.agent.interviewagent.constant.Constant.*;

@SpringBootTest
class SummarizeInterviewNodeTest {

    @Resource
    private SummarizeInterviewNode summarizeInterviewNode;
    @Resource
    private FluxRegistry registry;

    @Test
    void apply() {
        List<WorkExperience> workExperiences = Arrays.asList(
                WorkExperience.builder()
                        .companyName("阿里巴巴集团")
                        .responsibilities("负责电商平台核心交易系统的开发与维护，优化系统性能，参与架构升级")
                        .duration("2020.03 - 2023.06")
                        .build(),
                WorkExperience.builder()
                        .companyName("腾讯科技有限公司")
                        .responsibilities("参与微信支付后端模块开发，负责高并发场景下的接口优化和问题排查")
                        .duration("2018.07 - 2020.02")
                        .build()
        );

        // 构造项目列表
        List<Project> projects = Arrays.asList(
                Project.builder()
                        .projectName("双十一大促保障系统")
                        .duration("2021.09 - 2021.11")
                        .techStack(Arrays.asList("Spring Cloud", "Redis", "Kafka", "Docker"))
                        .description("负责设计并实现流量监控和弹性扩容模块，保障大促期间系统稳定运行，峰值QPS达到10万+")
                        .build(),
                Project.builder()
                        .projectName("智能客服机器人")
                        .duration("2022.01 - 2022.06")
                        .techStack(Arrays.asList("Python", "TensorFlow", "FastAPI", "MongoDB"))
                        .description("基于深度学习算法构建智能问答系统，实现意图识别和自动回复，准确率达90%以上")
                        .build()
        );

        // 构造完整的ResumeInfo对象
        ResumeInfo resumeInfo = ResumeInfo.builder()
                .name("张明")
                .job("高级Java开发工程师")
                .skills(Arrays.asList("Java", "Spring Boot", "MySQL", "Redis", "Kafka", "Docker", "Kubernetes"))
                .workExperiences(workExperiences)
                .projects(projects)
                .build();
        List<AnswerEvaluation> evaluationList = Arrays.asList(
                createEvaluation1(),
                createEvaluation2(),
                createEvaluation3(),
                createEvaluation4(),
                createEvaluation5(),
                createEvaluation6()
        );
        String evaluations = JSONUtil.toJsonStr(evaluationList);
        OverAllState state = new OverAllState();
        state.registerKeyAndStrategy(FLUX_ID, KeyStrategy.REPLACE);
        state.registerKeyAndStrategy(OUTPUT_INFO,KeyStrategy.REPLACE);
        state.registerKeyAndStrategy(EVALUATIONS,KeyStrategy.REPLACE);
        Map<String,Object> map = new HashMap<>();
        map.put(OUTPUT_INFO,resumeInfo);
        map.put(EVALUATIONS,evaluations);
        state.updateState(map);
        try {
            Map<String, Object> apply = summarizeInterviewNode.apply(state);
            String fluxId = apply.get(FLUX_ID).toString();
            Flux<GraphResponse<StreamingOutput>> generator = registry.get(fluxId);
            generator.doOnNext(resp -> {
                StreamingOutput streamingOutput = null;
                try {
                    streamingOutput = resp.getOutput().get();
                    String data = streamingOutput.getOriginData().toString();
                    System.out.println(data);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).blockLast();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 第1个评估 - 优秀
    private static AnswerEvaluation createEvaluation1() {
        AnswerEvaluation evaluation = new AnswerEvaluation();
        evaluation.setAnswer("Spring Boot微服务架构设计，使用Redis缓存和Kafka消息队列实现高并发处理");

        AnswerEvaluation.Evaluation eval = new AnswerEvaluation.Evaluation();
        eval.setTechnicalDepth(createScore(4.8));
        eval.setProblemSolving(createScore(4.5));
        eval.setCommunication(createScore(4.6));
        eval.setResumeConsistency(createScore(4.7));
        evaluation.setEvaluation(eval);
        evaluation.setOverallComment("技术深度优秀，能够清晰阐述复杂系统的架构设计，与简历描述高度一致");
        return evaluation;
    }

    // 第2个评估 - 良好
    private static AnswerEvaluation createEvaluation2() {
        AnswerEvaluation evaluation = new AnswerEvaluation();
        evaluation.setAnswer("通过索引优化和SQL重写，将慢查询从5秒降至200毫秒");

        AnswerEvaluation.Evaluation eval = new AnswerEvaluation.Evaluation();
        eval.setTechnicalDepth(createScore(4.0));
        eval.setProblemSolving(createScore(4.6));
        eval.setCommunication(createScore(4.2));
        eval.setResumeConsistency(createScore(4.0));
        evaluation.setEvaluation(eval);
        evaluation.setOverallComment("问题解决思路清晰，有具体的数据支撑，展现了较强的性能调优能力");
        return evaluation;
    }

    // 第3个评估 - 中等
    private static AnswerEvaluation createEvaluation3() {
        AnswerEvaluation evaluation = new AnswerEvaluation();
        evaluation.setAnswer("使用Docker容器化部署应用，配置了CI/CD流水线");

        AnswerEvaluation.Evaluation eval = new AnswerEvaluation.Evaluation();
        eval.setTechnicalDepth(createScore(3.5));
        eval.setProblemSolving(createScore(3.8));
        eval.setCommunication(createScore(4.0));
        eval.setResumeConsistency(createScore(3.5));
        evaluation.setEvaluation(eval);
        evaluation.setOverallComment("对DevOps工具有一定的了解，但缺乏深度实践经验和故障处理案例");
        return evaluation;
    }

    // 第4个评估 - 需要改进
    private static AnswerEvaluation createEvaluation4() {
        AnswerEvaluation evaluation = new AnswerEvaluation();
        evaluation.setAnswer("我熟悉Java基础语法，会使用Spring框架进行开发");

        AnswerEvaluation.Evaluation eval = new AnswerEvaluation.Evaluation();
        eval.setTechnicalDepth(createScore(2.5));
        eval.setProblemSolving(createScore(3.0));
        eval.setCommunication(createScore(3.5));
        eval.setResumeConsistency(createScore(2.5));
        evaluation.setEvaluation(eval);
        evaluation.setOverallComment("回答过于笼统，缺乏具体项目经验和技术深度，与简历中的高级工程师定位不符");
        return evaluation;
    }

    // 第5个评估 - 接近满分
    private static AnswerEvaluation createEvaluation5() {
        AnswerEvaluation evaluation = new AnswerEvaluation();
        evaluation.setAnswer("设计并实现了分布式链路追踪系统，基于SkyWalking和Elasticsearch，支持全链路监控和告警");

        AnswerEvaluation.Evaluation eval = new AnswerEvaluation.Evaluation();
        eval.setTechnicalDepth(createScore(4.9));
        eval.setProblemSolving(createScore(4.8));
        eval.setCommunication(createScore(4.7));
        eval.setResumeConsistency(createScore(4.9));
        evaluation.setEvaluation(eval);
        evaluation.setOverallComment("综合表现极佳，从方案设计到落地实施都有深入见解，展现了架构师思维");
        return evaluation;
    }

    // 第6个评估 - 沟通能力强
    private static AnswerEvaluation createEvaluation6() {
        AnswerEvaluation evaluation = new AnswerEvaluation();
        evaluation.setAnswer("在敏捷团队中负责需求分析和任务拆分，协调前后端联调，保证项目按时交付");

        AnswerEvaluation.Evaluation eval = new AnswerEvaluation.Evaluation();
        eval.setTechnicalDepth(createScore(3.2));
        eval.setProblemSolving(createScore(3.8));
        eval.setCommunication(createScore(4.8));
        eval.setResumeConsistency(createScore(4.0));
        evaluation.setEvaluation(eval);
        evaluation.setOverallComment("沟通能力突出，在项目管理方面表现良好，但技术深度有待加强");
        return evaluation;
    }

    // 辅助方法：创建Score对象
    private static AnswerEvaluation.Score createScore(double value) {
        AnswerEvaluation.Score score = new AnswerEvaluation.Score();
        score.setScore(value);
        return score;
    }

}