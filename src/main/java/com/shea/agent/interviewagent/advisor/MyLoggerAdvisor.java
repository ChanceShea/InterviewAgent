package com.shea.agent.interviewagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : Shea.
 * @since : 2026/7/19 19:17
 */
@Slf4j
public class MyLoggerAdvisor implements CallAdvisor, StreamAdvisor {
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        this.before(chatClientRequest);
        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);
        this.observeAfter(chatClientResponse);
        return chatClientResponse;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        this.before(chatClientRequest);
        List<ChatClientResponse> respList = new ArrayList<>();
        return streamAdvisorChain.nextStream(chatClientRequest)
                .doOnNext(respList::add)
                .doOnComplete(() -> {
                    log.info("Stream completed!");
                    this.aggregate(respList);
                })
                .doOnError(e -> {
                    log.error("Stream error：{}",e.getMessage(),e);
                    if (!respList.isEmpty()) {
                        this.aggregate(respList);
                    }
                });
    }

    private void aggregate(List<ChatClientResponse> respList) {
        if (respList.isEmpty()) {
            log.warn("No response chunks received");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (ChatClientResponse resp : respList) {
            String content = resp.chatResponse().getResult().getOutput().getText();
            if (content != null) {
                sb.append(content);
            }
        }
        log.info("AI Response: {}", sb);
    }

    private ChatClientRequest before(ChatClientRequest chatClientRequest) {
        log.debug("AI Request：{}",chatClientRequest.prompt());
        return chatClientRequest;
    }

    private void observeAfter(ChatClientResponse chatClientResponse) {
        log.info("AI Response：{}",chatClientResponse.chatResponse().getResult().getOutput().getText());
    }

    @Override
    public String getName() {
        return "myLoggerAdvisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
