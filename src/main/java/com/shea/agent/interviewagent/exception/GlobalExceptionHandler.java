package com.shea.agent.interviewagent.exception;

import cn.dev33.satoken.exception.NotLoginException;
import com.shea.agent.interviewagent.common.Result;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

/**
 * @author : Shea.
 * @since : 2026/6/6 14:46
 */
@RestControllerAdvice
@Slf4j
@Hidden
public class GlobalExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    public void notLoginExceptionHandler(NotLoginException e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.warn("Not login: {} - {}", request.getRequestURI(), e.getMessage());
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE)) {
            response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("event:error\ndata:{\"code\":401,\"message\":\"未登录或登录已过期\"}\n\n");
            response.getWriter().flush();
        } else {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\"}");
            response.getWriter().flush();
        }
    }

    @ExceptionHandler(PSQLException.class)
    public Result<?> duplicateKeyExceptionHandler(PSQLException e) {
        log.error("Duplicate key error:", e);
        return Result.fail(ErrorCode.SYSTEM_ERROR, "一个用户最多只能存储一份简历，请先删除上传过的简历");
    }

    @ExceptionHandler(BusinessException.class)
    public Result<?> businessExceptionHandler(BusinessException e) {
        log.error("Business error:", e);
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("Runtime error:", e);
        return Result.fail(ErrorCode.SYSTEM_ERROR, e.getMessage());
    }

}
