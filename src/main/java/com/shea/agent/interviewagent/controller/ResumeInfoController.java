package com.shea.agent.interviewagent.controller;

import com.shea.agent.interviewagent.common.Result;
import com.shea.agent.interviewagent.context.JobMatchContext;
import com.shea.agent.interviewagent.dto.ResumeInfoDTO;
import com.shea.agent.interviewagent.exception.BusinessException;
import com.shea.agent.interviewagent.exception.ErrorCode;
import com.shea.agent.interviewagent.service.IResumeInfoService;
import com.shea.agent.interviewagent.utils.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
public class ResumeInfoController {

    private final JobMatchContext context;
    private final IResumeInfoService resumeInfoService;

    @PostMapping("/pdf")
    public Result<String> uploadPDF(
            @RequestParam("file") MultipartFile file,
            @RequestParam String chatId
    ) throws IOException {
        String filePath = FileStorageUtil.saveTempFile(file);
        context.add(RESUME_PREFIX + chatId,filePath);
        return Result.success(filePath);
    }

    @PostMapping("/save")
    public Result<Boolean> saveResume(ResumeInfoDTO dto) {
        if (dto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"简历信息为空");
        }
        return Result.success(resumeInfoService.saveResume(dto));
    }

}
