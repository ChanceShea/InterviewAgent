package com.shea.agent.interviewagent.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.shea.agent.interviewagent.dto.LibraryExtractDTO;
import com.shea.agent.interviewagent.prompt.PromptHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author : Shea.
 * @since : 2026/8/20 14:48
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Context7SearchService {

    private final SyncMcpToolCallbackProvider mcpToolCallbackProvider;
    private final LlmService streamLlmService;
    private static final String TOOL_RESOLVE = "resolve_library_id";
    private static final String TOOL_QUERY = "query_docs";

    public String query(String query) {
        try{
            ToolCallback toolQuery = findTool(TOOL_QUERY);
            ToolCallback toolResolve = findTool(TOOL_RESOLVE);
            if (toolQuery == null || toolResolve == null) {
                log.warn("Context7 工具未加载");
                return null;
            }
            String prompt = PromptHelper.buildExtractLibraryNamePrompt(query);
            LibraryExtractDTO dto = streamLlmService.call("",prompt)
                    .mapNotNull(r -> r.getResult().getOutput().getText())
                    .collect(StringBuilder::new, StringBuilder::append)
                    .map(StringBuilder::toString)
                    .map(s -> JSONUtil.toBean(s, LibraryExtractDTO.class))
                    .block();
            if (dto == null || CollUtil.isEmpty(dto.getLibraries())) {
                log.info("LLM未从问题中提取到库名，跳过Context7。问题：{}",query);
                return null;
            }
            String libraryName = dto.getLibraries().getFirst();
            Map<String, Object> args = new HashMap<>();
            args.put("query", query);
            args.put("libraryName", libraryName);
            String argsJson = JSONUtil.toJsonStr(args);
            String resolve = toolResolve.call(argsJson);
            log.info("Context7 resolve-library-id 结果：{}", truncate(resolve,300));
            String libraryId = extractLibraryId(resolve);
            if (libraryId == null) {
                log.warn("未从resolve结果中提取到libraryId");
                return null;
            }
            Map<String, Object> qArgs = new HashMap<>();
            qArgs.put("libraryId", libraryId);
            qArgs.put("query", query);
            String queryArgs = JSONUtil.toJsonStr(qArgs);
            String result = toolQuery.call(queryArgs);
            log.info("Context7 查询结果：{}", truncate(result,300));
            return result;
        } catch (Exception e) {
            log.error("Context7 查询异常：{}",e.getMessage(), e);
            return null;
        }
    }

    private ToolCallback findTool(String toolName) {
        for (ToolCallback tc : mcpToolCallbackProvider.getToolCallbacks()) {
            if (tc.getToolDefinition().name().equals(toolName)) {
                return tc;
            }
        }
        return null;
    }

    /**
     * 从 resolve-library-id 的响应里提取第一个 /org/project 形式的 libraryId
     */
    private String extractLibraryId(String resolveJson) {
        if (resolveJson == null) return null;
        Matcher m = Pattern
                .compile("(/[A-Za-z0-9._-]+/[A-Za-z0-9._-]+(?:/[A-Za-z0-9._-]+)?)")
                .matcher(resolveJson);
        return m.find() ? m.group(1) : null;
    }

    private String truncate(String s, int n) {
        if (s == null) return "";
        return s.length() <= n ? s : s.substring(0, n) + "...";
    }
}
