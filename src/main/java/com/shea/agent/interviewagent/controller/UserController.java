package com.shea.agent.interviewagent.controller;


import cn.dev33.satoken.stp.StpUtil;
import com.shea.agent.interviewagent.common.Result;
import com.shea.agent.interviewagent.dto.UserLoginDTO;
import com.shea.agent.interviewagent.dto.UserRegisterDTO;
import com.shea.agent.interviewagent.exception.BusinessException;
import com.shea.agent.interviewagent.exception.ErrorCode;
import com.shea.agent.interviewagent.service.IUserService;
import com.shea.agent.interviewagent.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户主表，存储用户基本信息和登录凭证 前端控制器
 * </p>
 *
 * @author Shea
 * @since 2026-08-10
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @PostMapping("/register")
    public Result<Boolean> register(@RequestBody UserRegisterDTO dto) {
        if (dto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数不能为空");
        }
        return Result.success(userService.register(dto));
    }

    @PostMapping("/login")
    public Result<UserVO> login(@RequestBody UserLoginDTO dto) {
        if (dto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数不能为空");
        }
        return Result.success(userService.login(dto));
    }

    @GetMapping("/get/login")
    public Result<UserVO> getLoginUser(String uuid) {
        if (uuid == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数不能为空");
        }
        return Result.success(userService.getLoginUser(uuid));
    }


    @GetMapping("/logout")
    public Result<Boolean> logout() {
        StpUtil.logout();
        return Result.success(true);
    }
}
