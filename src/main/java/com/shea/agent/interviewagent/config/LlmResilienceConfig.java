package com.shea.agent.interviewagent.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : Shea.
 * @since : 2026/8/17 15:39
 */
@Configuration
public class LlmResilienceConfig {

    @Bean
    public CircuitBreaker llmCircuitBreaker() {
        return CircuitBreaker.ofDefaults("dashscope");
    }

    @Bean
    public RateLimiter rateLimiter() {
        return RateLimiter.ofDefaults("dashscope");
    }
}
