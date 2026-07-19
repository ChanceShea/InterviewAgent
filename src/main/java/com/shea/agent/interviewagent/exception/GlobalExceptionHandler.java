package com.shea.agent.interviewagent.exception;

import com.shea.agent.interviewagent.common.Result;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author : Shea.
 * @since : 2026/6/6 14:46
 */
@RestControllerAdvice
@Slf4j
@Hidden
public class GlobalExceptionHandler {

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
