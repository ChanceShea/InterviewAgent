package com.shea.agent.interviewagent.service.impl;

import com.shea.agent.interviewagent.dto.AgentQueryDTO;
import com.shea.agent.interviewagent.exception.BusinessException;
import com.shea.agent.interviewagent.exception.ErrorCode;
import com.shea.agent.interviewagent.service.AgentKnowledgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author : Shea.
 * @since : 2026/7/22 17:13
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AgentKnowledgeServiceImpl implements AgentKnowledgeService {

    private final VectorStore pgVectorStore;

    @Override
    public List<Document> queryDocuments(AgentQueryDTO dto) {
        if (dto.getQuery() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户提问不存在");
        }
        SearchRequest searchRequest = SearchRequest.builder()
                .query(dto.getQuery())
                .topK(dto.getTopK())
                .similarityThreshold(dto.getThreshold())
                .build();
        return pgVectorStore.similaritySearch(searchRequest);
    }
}
