package com.shea.agent.interviewagent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : Shea.
 * @since : 2026/7/21 21:37
 */
@Data
@NoArgsConstructor
public class QueryRewriteDTO {

    @JsonProperty("standalone_query")
    @JsonPropertyDescription("重写后的完整句子")
    private String standaloneQuery;
}
