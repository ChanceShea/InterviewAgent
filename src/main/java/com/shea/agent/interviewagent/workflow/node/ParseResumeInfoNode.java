package com.shea.agent.interviewagent.workflow.node;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.shea.agent.interviewagent.entity.ResumeInfo;
import com.shea.agent.interviewagent.prompt.PromptHelper;
import com.shea.agent.interviewagent.service.LlmService;
import com.shea.agent.interviewagent.utils.StateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.shea.agent.interviewagent.constant.Constant.*;

/**
 * @author : Shea.
 * @since : 2026/7/19 16:48
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ParseResumeInfoNode implements NodeAction {

    private final LlmService streamLlmService;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String filePath = StateUtil.getStringValue(state, INPUT_FILE);
        String chatId = StateUtil.getStringValue(state, CHAT_ID);
        List<Document> documents = loadDocument(filePath);
        String resumeText = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining());
        String prompt = PromptHelper.buildParseResumeInfoPrompt();
        Flux<ChatResponse> response = streamLlmService.call(prompt, resumeText, ResumeInfo.class);
        ResumeInfo info = response.mapNotNull(
                        r -> r.getResult().getOutput().getText()
                ).collect(StringBuilder::new, StringBuilder::append)
                .map(StringBuilder::toString)
                .filter(StrUtil::isNotBlank)
                .map(s -> JSONUtil.toBean(s, ResumeInfo.class))
                .block();
        log.info("成功解析简历：{}", info);
        return Map.of(
                OUTPUT_INFO,info,
                CHAT_ID,chatId
        );
    }

    private List<Document> loadDocument(String filePath) {
        Resource resource = new FileSystemResource(filePath);
        PagePdfDocumentReader reader = new PagePdfDocumentReader(resource);
        return reader.read();
    }
}
