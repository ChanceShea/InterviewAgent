package com.shea.agent.interviewagent.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;

/**
 * @author : Shea.
 * @since : 2026/8/17 14:52
 */
@Component
public class LlmConcurrencyGuard {

    private final Semaphore permits;

    public LlmConcurrencyGuard(
            @Value("${interview-agent.llm.max-concurrency:32}") int maxConcurrency
    ) {
        this.permits = new Semaphore(maxConcurrency);
    }

    public boolean tryAcquire() {
        return permits.tryAcquire();
    }

    public void release() {
        permits.release();
    }
}
