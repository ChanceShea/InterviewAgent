package com.shea.agent.interviewagent.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.shea.agent.interviewagent.common.Result;
import com.shea.agent.interviewagent.context.JobMatchContext;
import com.shea.agent.interviewagent.dto.ResumeInfoDTO;
import com.shea.agent.interviewagent.exception.BusinessException;
import com.shea.agent.interviewagent.exception.ErrorCode;
import com.shea.agent.interviewagent.service.IResumeInfoService;
import com.shea.agent.interviewagent.utils.FileStorageUtil;
import com.shea.agent.interviewagent.vo.ResumeInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.shea.agent.interviewagent.constant.Constant.RESUME_PREFIX;

/**
 * @author : Shea.
 * @since : 2026/7/31 08:50
 */
@RestController
@RequestMapping("/resume")
@RequiredArgsConstructor
@SaCheckLogin
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
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(resumeInfoService.saveResume(dto, userId));
    }

    @GetMapping("/list")
    public Result<List<ResumeInfoVO>> listResumeInfo(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户Id不能为空");
        }
        return Result.success(resumeInfoService.listResumeInfo(userId));
    }


}
