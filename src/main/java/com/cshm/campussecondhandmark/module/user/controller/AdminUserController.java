package com.cshm.campussecondhandmark.module.user.controller;

import com.cshm.campussecondhandmark.common.context.BaseContext;
import com.cshm.campussecondhandmark.common.result.PageResult;
import com.cshm.campussecondhandmark.common.result.Result;
import com.cshm.campussecondhandmark.module.user.pojo.dto.AdminUserBanDTO;
import com.cshm.campussecondhandmark.module.user.pojo.dto.AdminUserQueryDTO;
import com.cshm.campussecondhandmark.module.user.pojo.dto.AdminUserUnbanDTO;
import com.cshm.campussecondhandmark.module.user.pojo.dto.CampusVerifyAuditDTO;
import com.cshm.campussecondhandmark.module.user.pojo.dto.CampusVerifyQueryDTO;
import com.cshm.campussecondhandmark.module.user.pojo.vo.AdminUserDetailVO;
import com.cshm.campussecondhandmark.module.user.pojo.vo.AdminUserListVO;
import com.cshm.campussecondhandmark.module.user.pojo.vo.CampusVerifyAuditVO;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin-api/users")
@Slf4j
@Api(tags = "后台用户管理")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @ApiOperation(value = "分页查询后台用户列表", notes = "只查询普通用户，支持按账号状态与关键字筛选")
    @ApiImplicitParam(name = "token", value = "管理员登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<PageResult<AdminUserListVO>> pageAdminUsers(AdminUserQueryDTO dto) {
        log.info("后台分页查询用户列表");
        return Result.success(userService.pageAdminUsers(dto));
    }

    @GetMapping("/{userId}")
    @ApiOperation(value = "查询后台用户详情", notes = "按用户 ID 查询普通用户详情")
    @ApiImplicitParam(name = "token", value = "管理员登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<AdminUserDetailVO> getAdminUserDetail(
            @ApiParam(value = "用户 ID", required = true, example = "3") @PathVariable Long userId) {
        log.info("后台查询用户详情，用户ID={}", userId);
        return Result.success(userService.getAdminUserDetail(userId));
    }

    @PostMapping("/{userId}/ban")
    @ApiOperation(value = "封禁用户", notes = "仅允许封禁普通用户，封禁后立即生效")
    @ApiImplicitParam(name = "token", value = "管理员登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<Void> banUser(
            @ApiParam(value = "用户 ID", required = true, example = "3") @PathVariable Long userId,
            @ApiParam(value = "封禁请求", required = true) @RequestBody AdminUserBanDTO dto) {
        Long adminId = BaseContext.getCurrentId();
        log.info("后台封禁用户，管理员ID={}，用户ID={}", adminId, userId);
        userService.banUser(userId, adminId, dto);
        return Result.success();
    }

    @PostMapping("/{userId}/unban")
    @ApiOperation(value = "解封用户", notes = "仅允许解封已封禁的普通用户")
    @ApiImplicitParam(name = "token", value = "管理员登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<Void> unbanUser(
            @ApiParam(value = "用户 ID", required = true, example = "3") @PathVariable Long userId,
            @ApiParam(value = "解封请求", required = true) @RequestBody AdminUserUnbanDTO dto) {
        Long adminId = BaseContext.getCurrentId();
        log.info("后台解封用户，管理员ID={}，用户ID={}", adminId, userId);
        userService.unbanUser(userId, adminId, dto);
        return Result.success();
    }

    @GetMapping("/campus-verify/page")
    @ApiOperation(value = "分页查询校园认证审核列表", notes = "默认查询待审核用户，支持按认证状态与关键字过滤")
    @ApiImplicitParam(name = "token", value = "管理员登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<PageResult<CampusVerifyAuditVO>> pageCampusVerifyUsers(CampusVerifyQueryDTO dto) {
        log.info("后台分页查询校园认证审核列表");
        return Result.success(userService.pageCampusVerifyUsers(dto));
    }

    @PutMapping("/{userId}/campus-verify")
    @ApiOperation(value = "审核用户校园认证", notes = "支持通过或驳回用户校园认证")
    @ApiImplicitParam(name = "token", value = "管理员登录令牌", required = true, paramType = "header", dataTypeClass = String.class)
    public Result<Void> auditCampusVerify(
            @ApiParam(value = "用户 ID", required = true, example = "3") @PathVariable Long userId,
            @ApiParam(value = "校园认证审核请求", required = true) @RequestBody CampusVerifyAuditDTO dto) {
        Long adminId = BaseContext.getCurrentId();
        log.info("后台审核用户校园认证，管理员ID={}，用户ID={}，审核状态={}", adminId, userId, dto == null ? null : dto.getCampusVerifyStatus());
        userService.auditCampusVerify(userId, adminId, dto);
        return Result.success();
    }
}
