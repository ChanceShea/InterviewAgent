package com.shea.agent.interviewagent.vo;

import com.shea.agent.interviewagent.enums.EventType;
import lombok.Data;

/**
 * @author : Shea.
 * @since : 2026/8/3 14:11
 */
@Data
public class GraphNodeResponse {

    EventType event;
    String content;

    public GraphNodeResponse(EventType event, String content) {
        this.event = event;
        this.content = content;
    }

    public static GraphNodeResponse token(String content) {
        return new GraphNodeResponse(EventType.TOKEN, content);
    }

    public static GraphNodeResponse think(String content) {
        return new GraphNodeResponse(EventType.THINKING,content);
    }

    public static GraphNodeResponse done(int tokens) {
        return new GraphNodeResponse(EventType.DONE, "{\"totalTokens\":" + tokens + "}");
    }

    public static GraphNodeResponse error(String errMessage) {
        return new GraphNodeResponse(EventType.ERROR, errMessage);
    }

}
