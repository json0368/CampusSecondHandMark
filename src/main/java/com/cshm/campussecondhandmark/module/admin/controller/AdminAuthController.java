package com.cshm.campussecondhandmark.module.admin.controller;

import com.cshm.campussecondhandmark.common.result.Result;
import com.cshm.campussecondhandmark.module.admin.pojo.dto.AdminLoginDTO;
import com.cshm.campussecondhandmark.module.admin.pojo.vo.AdminLoginVO;
import com.cshm.campussecondhandmark.module.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin-api/auth")
@Slf4j
@Api(tags = "管理员认证")
public class AdminAuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @ApiOperation(value = "管理员登录", notes = "仅管理员账号可访问后台接口")
    public Result<AdminLoginVO> login(@ApiParam(value = "管理员登录请求", required = true) @RequestBody AdminLoginDTO adminLoginDTO) {
        log.info("管理员登录，邮箱={}", adminLoginDTO.getEmail());
        return Result.success(userService.adminLogin(adminLoginDTO));
    }
}
