package com.cshm.campussecondhandmark.module.user.controller;

import com.cshm.campussecondhandmark.common.context.BaseContext;
import com.cshm.campussecondhandmark.common.result.Result;
import com.cshm.campussecondhandmark.module.user.pojo.dto.UserLoginDTO;
import com.cshm.campussecondhandmark.module.user.pojo.dto.UserProfileUpdateDTO;
import com.cshm.campussecondhandmark.module.user.pojo.dto.UserRegisterDTO;
import com.cshm.campussecondhandmark.module.user.pojo.vo.CurrentUserVO;
import com.cshm.campussecondhandmark.module.user.pojo.vo.UserLoginVO;
import com.cshm.campussecondhandmark.module.user.pojo.vo.UserProfileVO;
import com.cshm.campussecondhandmark.module.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@Api(tags = "用户认证与资料")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/api/auth/login")
    @ApiOperation(value = "用户登录", notes = "当前版本仅支持邮箱加密码登录")
    public Result<UserLoginVO> login(@ApiParam(value = "用户登录请求", required = true) @RequestBody UserLoginDTO userLoginDTO) {
        log.info("用户登录，邮箱={}", userLoginDTO.getEmail());
        return Result.success(userService.login(userLoginDTO));
    }

    @PostMapping("/api/auth/register")
    @ApiOperation(value = "用户注册", notes = "注册成功后直接返回登录信息")
    public Result<UserLoginVO> register(@ApiParam(value = "用户注册请求", required = true) @RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("用户注册，用户名={}，邮箱={}", userRegisterDTO.getUsername(), userRegisterDTO.getEmail());
        return Result.success(userService.register(userRegisterDTO));
    }

    @GetMapping("/api/user/me")
    @ApiOperation("获取当前登录用户信息")
    @ApiImplicitParam(name = "token", value = "用户登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<CurrentUserVO> getCurrentUser() {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("获取当前登录用户信息，用户ID={}", currentUserId);
        return Result.success(userService.getCurrentUser(currentUserId));
    }

    @GetMapping("/api/users/{id}/profile")
    @ApiOperation("获取用户公开资料")
    public Result<UserProfileVO> getUserProfile(
            @ApiParam(value = "用户 ID", required = true, example = "1") @PathVariable Long id) {
        log.info("获取用户公开资料，用户ID={}", id);
        return Result.success(userService.getUserProfile(id));
    }

    @PutMapping("/api/user/profile")
    @ApiOperation("更新当前登录用户资料")
    @ApiImplicitParam(name = "token", value = "用户登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<Void> updateProfile(
            @ApiParam(value = "用户资料更新请求", required = true) @RequestBody UserProfileUpdateDTO userProfileUpdateDTO) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("更新当前登录用户资料，用户ID={}", currentUserId);
        userService.updateProfile(currentUserId, userProfileUpdateDTO);
        return Result.success();
    }
}
