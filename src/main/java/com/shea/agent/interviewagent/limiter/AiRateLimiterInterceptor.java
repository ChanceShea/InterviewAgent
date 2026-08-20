package com.shea.agent.interviewagent.limiter;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author : Shea.
 * @since : 2026/8/17 15:06
 */
@Component
@RequiredArgsConstructor
public class AiRateLimiterInterceptor implements HandlerInterceptor {

    private final RedissonClient redissonClient;
    @Value("${interview-agent.rate-limit.per-minute:10}")
    private int rateLimit;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // SSE 异步分发阶段线程上下文已丢失，鉴权与限流已在 REQUEST 阶段完成，直接放行
        if (request.getDispatcherType() == DispatcherType.ASYNC) {
            return true;
        }
        long userId = StpUtil.getLoginIdAsLong();
        RRateLimiter rateLimiter = redissonClient.getRateLimiter("rate:ai:" + userId);
        // 幂等设置，第一次创建，之后直接复用
        rateLimiter.trySetRate(RateType.PER_CLIENT,rateLimit,1, RateIntervalUnit.MINUTES);
        if (!rateLimiter.tryAcquire()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":429,\"msg\":\"请求过于频繁,请稍后再试\"}");
            return false;
        }
        return true;
    }
}
