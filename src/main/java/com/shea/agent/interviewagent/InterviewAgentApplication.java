package com.shea.agent.interviewagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InterviewAgentApplication {

    // TODO 用户上传简历后,可以持久化到数据库,面试分析时可以根据数据库中的简历直接开始面试
    public static void main(String[] args) {
        SpringApplication.run(InterviewAgentApplication.class, args);
    }

}
