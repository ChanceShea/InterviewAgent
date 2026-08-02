package com.shea.agent.interviewagent.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.shea.agent.interviewagent.common.Result;
import com.shea.agent.interviewagent.context.JobMatchContext;
import com.shea.agent.interviewagent.dto.JobUploadDTO;
import com.shea.agent.interviewagent.utils.RecognizeGeneralUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

import static com.shea.agent.interviewagent.constant.Constant.JD_PREFIX;

/**
 * @author : Shea.
 * @since : 2026/7/30 19:59
 */
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class JobController {

    private final RecognizeGeneralUtil recognizeGeneralUtil;
    private final JobMatchContext context;

    @PostMapping("/picture")
    public Result<String> uploadJobPicture(
            @RequestParam("file") MultipartFile file,
            @RequestParam("chatId") String chatId
    ) throws Exception {
        InputStream inputStream = file.getInputStream();
        String result = recognizeGeneralUtil.recognizeByStream(inputStream);
        JSONObject jsonObject = JSONUtil.parseObj(result);
        String inner = jsonObject.getJSONObject("body").getStr("data");
        JSONObject data = JSONUtil.parseObj(inner);
        String content = data.get("content").toString();
        context.add(JD_PREFIX + chatId,content);
        return Result.success(content);
    }

    @PostMapping("/text")
    public Result<String> uploadJobText(@RequestBody JobUploadDTO dto) {
        String content = dto.getJobDescription();
        context.add(JD_PREFIX + dto.getChatId(),content);
        return Result.success(content);
    }
}
