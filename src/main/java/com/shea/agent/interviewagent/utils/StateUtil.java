package com.shea.agent.interviewagent.utils;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.shea.agent.interviewagent.exception.BusinessException;
import com.shea.agent.interviewagent.exception.ErrorCode;

/**
 * @author : Shea.
 * @since : 2026/7/19 17:18
 */
public class StateUtil {

    public static  <T> T getObjectValue(OverAllState state,String key,Class<T> clazz,T defaultValue) {
        return state.value(key)
                .map(v -> {
                    if (v instanceof String) {
                        JSONObject jsonObject = JSONUtil.parseObj(v);
                        return JSONUtil.toBean(jsonObject, clazz);
                    }
                    return clazz.cast(v);
                }).orElse(defaultValue);
    }

    public static String getStringValue(OverAllState state,String key) {
        return state.value(key)
                .map(String.class::cast)
                .orElseThrow(
                        () -> new BusinessException(ErrorCode.PARAMS_ERROR,"状态键不存在：" + key)
                );
    }
}
