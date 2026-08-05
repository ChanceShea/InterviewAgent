package com.shea.agent.interviewagent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author : Shea.
 * @since : 2026/7/24 09:56
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphRequest {

    private MultipartFile file;

    private String query;

    private String chatId;

    private String phase;

    private String humanFeedbackContent;

    private boolean approved;
}
