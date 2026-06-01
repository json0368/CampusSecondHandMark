package com.cshm.campussecondhandmark.module.admin.controller;

import com.cshm.campussecondhandmark.common.result.Result;
import com.cshm.campussecondhandmark.common.service.OutlookOAuthTokenService;
import com.cshm.campussecondhandmark.module.admin.pojo.vo.OutlookOAuthAuthorizeVO;
import com.cshm.campussecondhandmark.module.admin.pojo.vo.OutlookOAuthCallbackVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin-api/mail/outlook/oauth")
@Slf4j
@Api(tags = "Outlook OAuth 发信初始化")
public class OutlookOAuthAdminController {

    @Autowired
    private OutlookOAuthTokenService outlookOAuthTokenService;

    @GetMapping("/authorize")
    @ApiOperation("获取 Outlook OAuth 授权地址")
    @ApiImplicitParam(name = "token", value = "管理员登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<OutlookOAuthAuthorizeVO> authorize() {
        log.info("生成 Outlook OAuth 授权地址");
        return Result.success(outlookOAuthTokenService.buildAuthorizeRequest());
    }

    @GetMapping("/callback")
    @ApiOperation("处理 Outlook OAuth 回调")
    public Result<OutlookOAuthCallbackVO> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @RequestParam(name = "error_description", required = false) String errorDescription) {
        log.info("处理 Outlook OAuth 回调");
        return Result.success(outlookOAuthTokenService.handleAuthorizationCallback(code, state, error, errorDescription));
    }
}
