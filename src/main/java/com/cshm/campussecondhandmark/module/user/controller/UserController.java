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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@Api(tags = "用户接口")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/api/auth/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("用户登录，邮箱={}", userLoginDTO.getEmail());
        return Result.success(userService.login(userLoginDTO));
    }

    @PostMapping("/api/auth/register")
    public Result<UserLoginVO> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("用户注册，用户名={}，邮箱={}", userRegisterDTO.getUsername(), userRegisterDTO.getEmail());
        return Result.success(userService.register(userRegisterDTO));
    }

    @GetMapping("/api/user/me")
    public Result<CurrentUserVO> getCurrentUser() {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("查询当前用户资料，用户id={}", currentUserId);
        return Result.success(userService.getCurrentUser(currentUserId));
    }

    @GetMapping("/api/users/{id}/profile")
    public Result<UserProfileVO> getUserProfile(@PathVariable Long id) {
        log.info("查询用户公开资料，用户id={}", id);
        return Result.success(userService.getUserProfile(id));
    }

    @PutMapping("/api/user/profile")
    public Result<Void> updateProfile(@RequestBody UserProfileUpdateDTO userProfileUpdateDTO) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("更新当前用户资料，用户id={}", currentUserId);
        userService.updateProfile(currentUserId, userProfileUpdateDTO);
        return Result.success();
    }
}
