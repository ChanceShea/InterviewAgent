package com.shea.agent.interviewagent.constant;

/**
 * @author : Shea.
 * @since : 2026/7/19 17:16
 */
public interface Constant {

    // region State键
    String INPUT_FILE = "file";

    String OUTPUT_INFO = "info";

    String CHAT_ID = "chatId";

    String FLUX_ID = "fluxId";

    String QUESTION = "questions";

    String INPUT_KEY = "input";

    String MULTI_TURN = "multi_turn";

    String ENHANCED_QUERY = "enhanced_query";

    String ANSWER_WITH_RAG = "answer_with_rag";

    String USER_REPLY_ANSWER = "user_reply_answer";

    String EVALUATIONS = "evaluations";

    String CURRENT_PHASE = "current_phase";

    String NEXT_STEP = "next_step";

    String INTERVIEW_SUMMARY = "interview_summary";

    String JOB_DESCRIPTION = "job_description";

    String MATCH_RESULT = "match_result";

    String FINAL_ANSWER = "final_answer";

    // endregion

    // region graph节点
    String INTERVIEW_AGENT_NAME = "interviewAgent";

    String PARSE_RESUME_INFO_NODE = "parseResumeInfoNode";

    String GENERATE_QUESTION_NODE = "generateQuestionNode";

    String ENHANCE_USER_QUERY_NODE = "enhanceUserQueryNode";

    String ANSWER_WITH_RAG_NODE = "answerWithRagNode";

    String EVALUATE_USER_QUERY_NODE = "evaluateUserQueryNode";

    String PLANNER_NODE = "plannerNode";

    String SUMMARIZE_INTERVIEW_NODE = "summarizeInterviewNode";

    String JOB_DESCRIPTION_SUMMARY_NODE = "jobDescriptionSummaryNode";

    String JOB_RESUME_MATCH_NODE = "jobResumeMatchNode";
    // endregion

    String FILE_PATH_PREFIX = "tmp/";

    String INTERVIEW_PHASE = "interview";

    String GENERAL_QUERY_PHASE = "generalQuery";

    String USER_MESSAGE = "USER";

    String ASSISTANT_MESSAGE = "ASSISTANT";

    String SYSTEM_MESSAGE = "SYSTEM";

    String JD_PREFIX = "Job-";

    String RESUME_PREFIX = "Resume-";

    String EVENT_COMPLETE = "complete";

    String EVENT_ERROR = "error";
}
