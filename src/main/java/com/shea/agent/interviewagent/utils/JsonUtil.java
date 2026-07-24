package com.shea.agent.interviewagent.utils;

import cn.hutool.core.util.StrUtil;

/**
 * 从流式输出中提取指定的json属性
 * @author : Shea.
 * @since : 2026/7/24 20:48
 */
public class JsonUtil {

    public static String extractField(String res,String fieldName) {
        if (StrUtil.isBlank(res)) {
            return null;
        }
        return extractFieldFromJsonFragment(res,fieldName);
    }

    /**
     * 从部分json中提取指定属性的值
     */
    private static String extractFieldFromJsonFragment(String json, String fieldName) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        int fieldIndex = json.indexOf("\"" + fieldName + "\"");
        if (fieldIndex < 0) {
            return null;
        }
        int colonIndex = json.indexOf(":", fieldIndex);
        if (colonIndex < 0) {
            return null;
        }
        int startQuote = json.indexOf("\"", colonIndex);
        if (startQuote < 0) {
            return null;
        }
        int start = startQuote + 1;
        int endQuote = findEndQuote(json,start);
        if (endQuote < 0) {
            // 没有找到结束引号，说明json被截断了，返回从开始到末尾的所有内容
            String value = json.substring(start);
            return unescapeJson(value);
        }
        String value = json.substring(start, endQuote);
        return unescapeJson(value);
    }

    /**
     * 找到结尾引号
     * @param json json字符串
     * @param start 开始位置
     * @return 结尾引号索引
     */
    private static int findEndQuote(String json, int start) {
        int i = start;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '\\') {
                // 跳过转义字符及其后面的字符
                i += 2;
                continue;
            }
            if (c == '"') {
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * 处理json转义字符
     * @param value json字符串
     * @return 处理后的json字符串
     */
    private static String unescapeJson(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < value.length()) {
            char c = value.charAt(i);
            if (c == '\\' && i + 1 < value.length()) {
                char next = value.charAt(i + 1);
                switch (next) {
                    case 'n':
                        result.append('\n');
                        i += 2;
                        break;
                    case 't':
                        result.append('\t');
                        i += 2;
                        break;
                    case 'r':
                        result.append('\r');
                        i += 2;
                        break;
                    case 'b':
                        result.append('\b');
                        i += 2;
                        break;
                    case 'f':
                        result.append('\f');
                        i += 2;
                        break;
                    case '"':
                        result.append('"');
                        i += 2;
                        break;
                    case '\\':
                        result.append('\\');
                        i += 2;
                        break;
                    case 'u':
                        // Unicode 转义（如 \u4f60）
                        if (i + 5 < value.length()) {
                            try {
                                String hex = value.substring(i + 2, i + 6);
                                int codePoint = Integer.parseInt(hex, 16);
                                result.append((char) codePoint);
                                i += 6;
                            } catch (NumberFormatException e) {
                                result.append(value.charAt(i));
                                i++;
                            }
                        } else {
                            result.append(value.charAt(i));
                            i++;
                        }
                        break;
                    default:
                        result.append(next);
                        i += 2;
                }
            } else {
                result.append(c);
                i++;
            }
        }
        return result.toString();
    }
}
