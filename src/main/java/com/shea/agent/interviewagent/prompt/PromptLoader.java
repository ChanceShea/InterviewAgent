package com.shea.agent.interviewagent.prompt;

import com.shea.agent.interviewagent.exception.BusinessException;
import com.shea.agent.interviewagent.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prompt加载器
 * @author : Shea.
 * @since : 2026/7/19 17:44
 */
@Slf4j
public class PromptLoader {

    private PromptLoader() {}

    private static final String PROMPT_PATH_PREFIX = "prompts/";
    private static final ConcurrentHashMap<String,String> promptCache = new ConcurrentHashMap<>();

    public static String loadPrompt(String promptName) {
        return promptCache.computeIfAbsent(promptName,name -> {
            String fileName = PROMPT_PATH_PREFIX + name;
            try (InputStream in = PromptLoader.class.getClassLoader().getResourceAsStream(fileName)) {
                return StreamUtils.copyToString(in,StandardCharsets.UTF_8);
            }catch (IOException e) {
                log.error("提示词加载失败：{}",e.getMessage(),e);
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"提示词加载失败");
            }
        });
    }
}
