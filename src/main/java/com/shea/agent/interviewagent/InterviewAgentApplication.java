package com.shea.agent.interviewagent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.shea.agent.interviewagent.mapper")
public class InterviewAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterviewAgentApplication.class, args);
    }

}
