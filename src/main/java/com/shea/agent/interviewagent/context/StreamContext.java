package com.shea.agent.interviewagent.context;

import com.shea.agent.interviewagent.vo.GraphNodeResponse;
import lombok.Data;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.Disposable;
import reactor.core.publisher.Sinks;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 流式处理上下文，封装相关状态
 * @author : Shea.
 * @since : 2026/7/24 10:12
 */
@Data
public class StreamContext {

    private String chatId;

    private Sinks.Many<ServerSentEvent<GraphNodeResponse>> sink;

    private String finalAnswer;

    private Disposable disposable;

    /**
     * 累计token用量
     */
    private final AtomicInteger totalTokens = new AtomicInteger(0);

    /**
     * 用于标记是否清理，防止重复清理
     */
    private final AtomicBoolean cleaned = new AtomicBoolean(false);

    /**
     * 清理全部资源，确保线程安全
     */
    public void cleanup() {
        // 使用 compareAndSet 确保只执行一次清理
        if (!cleaned.compareAndSet(false, true)) {
            return;
        }

        // 清理 Disposable
        Disposable localDisposable = disposable;
        if (localDisposable != null && !localDisposable.isDisposed()) {
            try {
                localDisposable.dispose();
            }
            catch (Exception e) {
                // 忽略清理过程中的异常
            }
        }

        // 清理 Sink
        Sinks.Many<ServerSentEvent<GraphNodeResponse>> localSink = sink;
        if (localSink != null) {
            try {
                localSink.tryEmitComplete();
            }
            catch (Exception e) {
                // 忽略清理过程中的异常
            }
        }
    }

    public boolean isCleaned() {
        return cleaned.get();
    }
}
