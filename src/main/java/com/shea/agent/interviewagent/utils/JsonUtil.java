package com.shea.agent.interviewagent.utils;

import cn.hutool.core.util.StrUtil;

/**
 * 从流式输出中提取指定的json属性
 * @author : Shea.
 * @since : 2026/7/24 20:48
 */
public class JsonUtil {

    public static String extractField(String res, String fieldName) {
        if (StrUtil.isBlank(res)) {
            return null;
        }
        if (fieldName.contains(".")) {
            return extractNestedField(res, fieldName);
        }
        return extractFieldFromJsonFragment(res, fieldName);
    }

    /**
     * 从json中提取嵌套字段，支持点分路径如 "summary.technical_overview"
     */
    private static String extractNestedField(String json, String path) {
        String[] segments = path.split("\\.");
        String current = json;
        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            String value = extractFieldValue(current, segment);
            if (value == null) {
                return null;
            }
            if (i == segments.length - 1) {
                return value;
            }
            // 中间节点必须是对象或数组才能继续深入
            value = value.trim();
            if (value.startsWith("{") || value.startsWith("[")) {
                current = value;
            } else {
                return null;
            }
        }
        return null;
    }

    /**
     * 从json片段中提取字段的原始值（可能是字符串、对象、数组）
     */
    private static String extractFieldValue(String json, String fieldName) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        int fieldIndex = json.indexOf("\"" + fieldName + "\"");
        if (fieldIndex < 0) {
            return null;
        }
        int colonIndex = json.indexOf(":", fieldIndex + fieldName.length() + 2);
        if (colonIndex < 0) {
            return null;
        }
        // 跳过空白
        int pos = colonIndex + 1;
        while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
            pos++;
        }
        if (pos >= json.length()) {
            return null;
        }
        char c = json.charAt(pos);
        if (c == '"') {
            // 字符串值
            return extractStringValue(json, pos);
        } else if (c == '{') {
            // 对象值
            return extractBalancedBlock(json, pos, '{', '}');
        } else if (c == '[') {
            // 数组值
            return extractBalancedBlock(json, pos, '[', ']');
        } else {
            // 数字、布尔、null — 读到下一个结构字符
            return extractPrimitiveValue(json, pos);
        }
    }

    /**
     * 提取字符串值，处理转义引号
     */
    private static String extractStringValue(String json, int quotePos) {
        int start = quotePos + 1;
        int end = findEndQuote(json, start);
        if (end < 0) {
            // 字符串被截断，返回从start到末尾的内容
            return unescapeJson(json.substring(start));
        }
        return unescapeJson(json.substring(start, end));
    }

    /**
     * 提取括号平衡的块（对象或数组），处理嵌套和字符串中的括号
     */
    private static String extractBalancedBlock(String json, int start, char open, char close) {
        int depth = 0;
        int i = start;
        boolean inString = false;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (inString) {
                if (c == '\\') {
                    i += 2;
                    continue;
                }
                if (c == '"') {
                    inString = false;
                }
            } else {
                if (c == '"') {
                    inString = true;
                } else if (c == open) {
                    depth++;
                } else if (c == close) {
                    depth--;
                    if (depth == 0) {
                        return json.substring(start, i + 1);
                    }
                }
            }
            i++;
        }
        // 括号未闭合（流式截断），返回到末尾
        return json.substring(start);
    }

    /**
     * 提取原始类型值（数字、布尔、null）
     */
    private static String extractPrimitiveValue(String json, int start) {
        int i = start;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == ',' || c == '}' || c == ']' || Character.isWhitespace(c)) {
                break;
            }
            i++;
        }
        return json.substring(start, i);
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
