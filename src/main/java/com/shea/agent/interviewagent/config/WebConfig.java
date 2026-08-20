package com.shea.agent.interviewagent.config;

import com.shea.agent.interviewagent.limiter.AiRateLimiterInterceptor;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author : Shea.
 * @since : 2026/8/3 19:07
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AiRateLimiterInterceptor aiRateLimiterInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(aiRateLimiterInterceptor)
                .addPathPatterns("/ai/chat","/ai/match");
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(asyncTaskExecutor());
        // SSE 流式接口需容忍长时间流（LLM 多节点串行），放宽默认 30s 超时
        configurer.setDefaultTimeout(120000);
    }

    @Bean
    public ThreadPoolTaskExecutor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }

    @Bean
    public ExecutorService executorService() {
        return new ThreadPoolExecutor(
                16, 32, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(500),
                new DefaultThreadFactory("Async-Thread-"),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

}
