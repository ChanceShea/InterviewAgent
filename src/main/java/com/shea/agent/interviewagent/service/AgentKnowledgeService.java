package com.shea.agent.interviewagent.service;

import com.shea.agent.interviewagent.dto.AgentQueryDTO;
import org.springframework.ai.document.Document;

import java.util.List;

/**
 * @author : Shea.
 * @since : 2026/7/22 17:13
 */
public interface AgentKnowledgeService {

    List<Document> queryDocuments(AgentQueryDTO dto);
}
