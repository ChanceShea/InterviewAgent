package com.shea.agent.interviewagent.controller;

import com.shea.agent.interviewagent.common.Result;
import com.shea.agent.interviewagent.context.JobMatchContext;
import com.shea.agent.interviewagent.utils.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.shea.agent.interviewagent.constant.Constant.RESUME_PREFIX;

/**
 * @author : Shea.
 * @since : 2026/7/31 08:50
 */
@RestController
@RequestMapping("/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final JobMatchContext context;

    @PostMapping("/pdf")
    public Result<String> uploadPDF(
            @RequestParam("file") MultipartFile file,
            @RequestParam String chatId
    ) throws IOException {
        String filePath = FileStorageUtil.saveTempFile(file);
        context.add(RESUME_PREFIX + chatId,filePath);
        return Result.success(filePath);
    }


}
