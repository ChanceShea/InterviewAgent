package com.shea.agent.interviewagent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @author : Shea.
 * @since : 2026/8/20 15:58
 */
@Data
public class LibraryExtractDTO {

    @JsonProperty("libraries")
    private List<String> libraries;
}
