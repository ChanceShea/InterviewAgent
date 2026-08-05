package com.shea.agent.interviewagent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.shea.agent.interviewagent.mapper")
public class InterviewAgentApplication {

    // TODO 用户上传简历后,可以持久化到数据库,面试分析时可以根据数据库中的简历直接开始面试
    // TODO RAG之前可利用LLM模拟一段回复，然后拿着回复去检索
    public static void main(String[] args) {
        SpringApplication.run(InterviewAgentApplication.class, args);
    }

}
