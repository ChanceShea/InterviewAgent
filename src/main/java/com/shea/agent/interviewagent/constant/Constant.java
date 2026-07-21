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

    String QUESTIONS = "questions";
    // endregion

    // region graph节点
    String INTERVIEW_AGENT_NAME = "interviewAgent";

    String PARSE_RESUME_INFO_NODE = "parseResumeInfoNode";

    String GENERATE_QUESTION_NODE = "generateQuestionNode";
    // endregion

    String FILE_PATH_PREFIX = "tmp/";

}
