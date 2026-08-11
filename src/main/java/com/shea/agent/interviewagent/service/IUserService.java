package com.shea.agent.interviewagent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shea.agent.interviewagent.dto.UserLoginDTO;
import com.shea.agent.interviewagent.dto.UserRegisterDTO;
import com.shea.agent.interviewagent.entity.User;
import com.shea.agent.interviewagent.vo.UserVO;

/**
 * <p>
 * 用户主表，存储用户基本信息和登录凭证 服务类
 * </p>
 *
 * @author Shea
 * @since 2026-08-10
 */
public interface IUserService extends IService<User> {

    UserVO login(UserLoginDTO dto);

    Boolean register(UserRegisterDTO dto);

    UserVO getLoginUser(String uuid);
}
