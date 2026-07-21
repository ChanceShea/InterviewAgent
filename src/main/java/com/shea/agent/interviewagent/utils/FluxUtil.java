package com.shea.agent.interviewagent.utils;

import com.alibaba.cloud.ai.graph.GraphResponse;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.streaming.OutputType;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author : Shea.
 * @since : 2026/7/20 16:19
 */
public final class FluxUtil {

    private FluxUtil() {}


    public static Flux<GraphResponse<StreamingOutput>> createStreamingGenerator(
            Class<? extends NodeAction> nodeClass, OverAllState state,Flux<ChatResponse> sourceFlux,
            Flux<ChatResponse> preFlux,Flux<ChatResponse> sufFlux, Function<String, Map<String,Object>> sourceMapper
    ) {
        String nodeName = nodeClass.getSimpleName();
        final StringBuilder sb = new StringBuilder();
        sourceFlux = sourceFlux.doOnNext(r -> sb.append(ChatResponseUtil.getText(r)));
        return toStreamingResponseFlux(nodeName,state,Flux.concat(preFlux,sourceFlux,sufFlux),() -> sourceMapper.apply(sb.toString()));
    }

    private static Flux<GraphResponse<StreamingOutput>> toStreamingResponseFlux(
            String nodeName,
            OverAllState state,
            Flux<ChatResponse> sourceFlux,
            Supplier<Map<String,Object>> resultSupplier
    ) {
        Flux<GraphResponse<StreamingOutput>> streamingFlux = sourceFlux.filter(
                resp -> resp != null &&
                        resp.getResult() != null &&
                        resp.getResult().getOutput() != null
                ).map(resp -> GraphResponse.of(
                                        new StreamingOutput<>(
                                                resp.getResult().getOutput(),
                                                resp,
                                                nodeName,
                                                "",
                                                state,
                                                OutputType.from(true,nodeName)
                                        )));
        return streamingFlux.concatWith(
                    Mono.fromSupplier(() -> GraphResponse.done(resultSupplier.get()))
                ).onErrorResume(err -> Flux.just(GraphResponse.error(err)));
    }
}
