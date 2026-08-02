package com.shea.agent.interviewagent.controller;

import cn.hutool.core.util.StrUtil;
import com.shea.agent.interviewagent.common.Result;
import com.shea.agent.interviewagent.dto.JobResumeMatchDTO;
import com.shea.agent.interviewagent.exception.BusinessException;
import com.shea.agent.interviewagent.exception.ErrorCode;
import com.shea.agent.interviewagent.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

/**
 * @author : Shea.
 * @since : 2026/7/20 17:44
 */
@RestController
@RequestMapping("/ai")
@Slf4j
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;


    @PostMapping(value = "/chat",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(
            @RequestParam(value = "file",required = false) MultipartFile file,
            @RequestParam(value = "input",required = false) String input,
            @RequestParam("chatId") String chatId,
            @RequestParam("phase") String phase
    ) {
        return aiService.chat(file, input, chatId,phase);
    }

    @GetMapping("/match")
    public Result<JobResumeMatchDTO> jdMatch(@RequestParam String chatId) {
        if (StrUtil.isBlank(chatId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return Result.success(aiService.jdMatch(chatId));
    }
}
