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

    String INPUT_KEY = "input";

    String MULTI_TURN_QUERY = "multi_turn_query";

    String ENHANCED_QUERY = "enhanced_query";
    // endregion

    // region graph节点
    String INTERVIEW_AGENT_NAME = "interviewAgent";

    String PARSE_RESUME_INFO_NODE = "parseResumeInfoNode";

    String GENERATE_QUESTION_NODE = "generateQuestionNode";

    String ENHANCE_USER_QUERY_NODE = "enhanceUserQueryNode";
    // endregion

    String FILE_PATH_PREFIX = "tmp/";

}
