package com.shea.agent.interviewagent.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shea.agent.interviewagent.dto.UserLoginDTO;
import com.shea.agent.interviewagent.dto.UserRegisterDTO;
import com.shea.agent.interviewagent.entity.User;
import com.shea.agent.interviewagent.enums.UserStatusEnum;
import com.shea.agent.interviewagent.exception.BusinessException;
import com.shea.agent.interviewagent.exception.ErrorCode;
import com.shea.agent.interviewagent.mapper.UserMapper;
import com.shea.agent.interviewagent.service.IUserService;
import com.shea.agent.interviewagent.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户主表，存储用户基本信息和登录凭证 服务实现类
 * </p>
 *
 * @author Shea
 * @since 2026-08-10
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public UserVO login(UserLoginDTO dto) {
        String username = dto.getUsername();
        String password = dto.getPassword();
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号或密码不能为空");
        }
        LambdaQueryWrapper<User> exist = Wrappers.lambdaQuery(User.class)
                .eq(User::getUsername, username)
                .eq(User::getStatus, UserStatusEnum.ENABLED.getValue());
        User user = this.getOne(exist);
        if (user == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"用户被封禁或不存在");
        }
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名或密码错误");
        }
        StpUtil.login(user.getId());
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user,vo);
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        String token = tokenInfo.getTokenValue();
        vo.setToken(token);
        return vo;
    }

    @Override
    public Boolean register(UserRegisterDTO dto) {
        String username = dto.getUsername();
        String password = dto.getPassword();
        String confirmPassword = dto.getConfirmPassword();
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password) || StrUtil.isBlank(confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名或密码不能为空");
        }
        LambdaQueryWrapper<User> eq = Wrappers.lambdaQuery(User.class)
                .eq(User::getUsername, username)
                .eq(User::getStatus, UserStatusEnum.ENABLED.getValue());
        User user = this.getOne(eq);
        if (user != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户已存在");
        }
        user = new User();
        String uuid = RandomUtil.randomString(10);
        BeanUtils.copyProperties(dto,user);
        user.setUuid(uuid);
        String hashpw = BCrypt.hashpw(password, BCrypt.gensalt());
        user.setPassword(hashpw);
        user.setStatus(UserStatusEnum.ENABLED.getValue());
        boolean saved = this.save(user);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"数据库操作失败");
        }
        return true;
    }

    @Override
    public UserVO getLoginUser(String uuid) {
        LambdaQueryWrapper<User> eq = Wrappers.lambdaQuery(User.class)
                .eq(User::getUuid, uuid)
                .eq(User::getStatus, UserStatusEnum.ENABLED.getValue());
        User user = this.getOne(eq);
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user,vo);
        return vo;
    }
}
