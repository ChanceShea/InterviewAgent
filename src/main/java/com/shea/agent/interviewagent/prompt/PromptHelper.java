package com.shea.agent.interviewagent.prompt;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.shea.agent.interviewagent.dto.*;
import org.springframework.ai.converter.BeanOutputConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : Shea.
 * @since : 2026/7/19 17:53
 */
public class PromptHelper {

    public static String buildParseResumeInfoPrompt() {
        Map<String,Object> params = new HashMap<>();
        BeanOutputConverter<ResumeInfoDTO> converter = new BeanOutputConverter<>(ResumeInfoDTO.class);
        String format = converter.getFormat();
        params.put("format",format);
        return PromptConstant.getParseResumeInfoPrompt().render(params);
    }

    public static String buildGenerateQuestionPrompt(ResumeInfoDTO resumeInfo, String multiTurn) {
        JSONObject resume = JSONUtil.parseObj(resumeInfo);
        Map<String,Object> params = new HashMap<>(resume);
        params.put("resumeInfo",resume);
        params.put("multi_turn",multiTurn);
        return PromptConstant.getGenerateQuestionPrompt().render(params);
    }

    public static String buildEnhanceUserPrompt(String multiTurn,String latestQuery) {
        Map<String,Object> params = new HashMap<>();
        params.put("multi_turn", multiTurn != null ? multiTurn : "(无)");
        params.put("latest_query", latestQuery);
        BeanOutputConverter<QueryRewriteDTO> beanOutputConverter = new BeanOutputConverter<>(QueryRewriteDTO.class);
        params.put("format", beanOutputConverter.getFormat());
        return PromptConstant.getEnhanceUserPrompt().render(params);
    }

    public static String buildAnswerWithRagPrompt(String documents,String multiTurn,String enhancedQuery) {
        Map<String,Object> params = new HashMap<>();
        params.put("documents", documents);
        params.put("multi_turn", multiTurn);
        params.put("enhanced_query", enhancedQuery);
        BeanOutputConverter<AnswerUserQueryDTO> beanOutputConverter = new BeanOutputConverter<>(AnswerUserQueryDTO.class);
        params.put("format", beanOutputConverter.getFormat());
        return PromptConstant.getAnswerWithRagPrompt().render(params);
    }

    public static String buildEvaluateUserAnswerPrompt(ResumeInfoDTO info,String currentQuestion,String userAnswer,String multiTurn) {
        JSONObject resume = JSONUtil.parseObj(info);
        Map<String, Object> params = new HashMap<>(resume);
        params.put("current_question", currentQuestion);
        params.put("user_answer", userAnswer);
        params.put("multi_turn", multiTurn);
        return PromptConstant.getEvaluateUserAnswerPrompt().render(params);
    }

    public static String buildInterviewPlannerPrompt(ResumeInfoDTO info, List<AnswerEvaluation> answerEvaluations) {
        JSONObject resume = JSONUtil.parseObj(info);
        JSONArray evaluations = JSONUtil.parseArray(answerEvaluations);
        Map<String, Object> params = new HashMap<>(resume);
        params.put("evaluations", evaluations);
        params.put("questionCount",evaluations.size());
        return PromptConstant.getInterviewPlannerPrompt().render(params);
    }

    public static String buildSummarizeInterviewPrompt(ResumeInfoDTO info,String answerEvaluations) {
        JSONObject jsonObject = JSONUtil.parseObj(info);
        JSONArray evaluations = JSONUtil.parseArray(answerEvaluations);
        Map<String, Object> params = new HashMap<>(jsonObject);
        params.put("evaluations", evaluations);
        return PromptConstant.getSummarizeInterviewPrompt().render(params);
    }

    public static String buildJobDescriptionSummaryPrompt(String content) {
        Map<String,Object> params = new HashMap<>();
        params.put("job_description", content);
        return PromptConstant.getJobDescriptionSummaryPrompt().render(params);
    }

    public static String buildJobResumeMatchPrompt(String jobDescription,String resumeInfo) {
        Map<String,Object> params = new HashMap<>();
        params.put("job_description", jobDescription);
        params.put("resume_info", resumeInfo);
        return PromptConstant.getJobResumeMatchPrompt().render(params);
    }

    public static String buildExtractLibraryNamePrompt(String question) {
        Map<String,Object> params = new HashMap<>();
        params.put("user_question",question);
        return PromptConstant.getExtractLibraryNamePrompt().render(params);
    }
}
