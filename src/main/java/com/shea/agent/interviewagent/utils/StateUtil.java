package com.shea.agent.interviewagent.utils;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.shea.agent.interviewagent.exception.BusinessException;
import com.shea.agent.interviewagent.exception.ErrorCode;

import java.util.List;

/**
 * @author : Shea.
 * @since : 2026/7/19 17:18
 */
public class StateUtil {

    public static <T> T getObjectValue(OverAllState state,String key,Class<T> clazz) {
        return state.value(key)
                .map(v -> {
                    if (v instanceof String) {
                        JSONObject jsonObject = JSONUtil.parseObj(v);
                        return JSONUtil.toBean(jsonObject, clazz);
                    }
                    return clazz.cast(v);
                }).orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "状态键: " + key + " 不存在"));
    }

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

    public static String getStringValue(OverAllState state,String key,String defaultValue) {
        return state.value(key)
                .map(String.class::cast)
                .orElse(defaultValue);
    }

    public static Long getLongValue(OverAllState state, String key) {
        return state.value(key)
                .map(v -> {
                    if (v instanceof Long l) return l;
                    if (v instanceof Number n) return n.longValue();
                    return Long.parseLong(v.toString());
                })
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "状态键不存在：" + key));
    }

    public static <T> List<T> getListValue(OverAllState state, String key, Class<T> elementType, List<T> defaultValue) {
        return state.value(key)
                .map(v -> {
                    if (v instanceof String) {
                        JSONArray jsonArray = JSONUtil.parseArray(v);
                        return jsonArray.toList(elementType);
                    }
                    return (List<T>) v;
                }).orElse(defaultValue);
    }
}
