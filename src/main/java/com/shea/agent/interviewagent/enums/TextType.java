package com.shea.agent.interviewagent.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author : Shea.
 * @since : 2026/7/20 16:41
 */
@AllArgsConstructor
@Getter
public enum TextType {

    JSON("$$$json", "$$$"),

    PYTHON("$$$python", "$$$"),

    // LLM模型爱输出```sql，那就换一个标记
    SQL("$$$sql", "$$$"),

    MARK_DOWN("$$$markdown-report", "$$$/markdown-report"),

    RESULT_SET("$$$result_set", "$$$"),

    TEXT(null, null);

    private final String startSign;

    private final String endSign;

    public static TextType getType(TextType origin, String chuck) {
        if (origin == TEXT) {
            for (TextType type : TextType.values()) {
                if (chuck.equals(type.startSign)) {
                    return type;
                }
            }
        }
        else {
            if (chuck.equals(origin.endSign)) {
                return TextType.TEXT;
            }
        }
        return origin;
    }

    public static TextType getTypeByStratSign(String startSign) {
        for (TextType type : TextType.values()) {
            if (startSign.equals(type.startSign)) {
                return type;
            }
        }
        return TextType.TEXT;
    }

}

