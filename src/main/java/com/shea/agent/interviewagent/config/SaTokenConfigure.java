package com.shea.agent.interviewagent.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author : Shea.
 * @since : 2026/8/10 17:04
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                // 【核心修复】如果是异步分发（ASYNC），直接放行
                // 异步分发阶段线程上下文已丢失，但鉴权已在 REQUEST 阶段完成
                if (request.getDispatcherType() == DispatcherType.ASYNC) {
                    return true;
                }

                // 以下为正常鉴权逻辑，仅在 REQUEST 阶段执行
                SaRouter.match("/**")
                        .notMatch("/user/login","/user/register", "/error", "/swagger-ui/**", "/v3/api-docs/**")
                        .check(r -> StpUtil.checkLogin());

                return true;
            }
        }).addPathPatterns("/**");
    }
}
