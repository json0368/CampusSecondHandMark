package com.cshm.campussecondhandmark.module.user.pojo.vo;

import com.cshm.campussecondhandmark.module.user.enums.CampusVerifyStatusEnum;
import com.cshm.campussecondhandmark.module.user.enums.UserRoleEnum;
import com.cshm.campussecondhandmark.module.user.enums.UserStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "后台用户详情信息")
public class AdminUserDetailVO {

    @ApiModelProperty(value = "用户 ID", example = "3")
    private Long id;

    @ApiModelProperty(value = "用户名", example = "zhangsan")
    private String username;

    @ApiModelProperty(value = "昵称", example = "张三")
    private String nickname;

    @ApiModelProperty(value = "邮箱", example = "zhangsan@example.com")
    private String email;

    @ApiModelProperty(value = "手机号", example = "13800000000")
    private String phone;

    @ApiModelProperty(value = "学号", example = "20240001")
    private String studentNo;

    @ApiModelProperty(value = "专业", example = "软件工程")
    private String major;

    @ApiModelProperty(value = "头像 URL", example = "http://example.com/avatar.jpg")
    private String avatarUrl;

    @ApiModelProperty(value = "角色", example = "USER")
    private UserRoleEnum role;

    @ApiModelProperty(value = "账号状态", example = "NORMAL")
    private UserStatusEnum status;

    @ApiModelProperty(value = "校园认证状态", example = "APPROVED")
    private CampusVerifyStatusEnum campusVerifyStatus;

    @ApiModelProperty(value = "封禁原因", example = "发布违规商品")
    private String banReason;

    @ApiModelProperty(value = "封禁时间")
    private LocalDateTime banTime;

    @ApiModelProperty(value = "封禁管理员 ID", example = "7")
    private Long banAdminId;

    @ApiModelProperty(value = "解封原因", example = "已完成整改")
    private String unbanReason;

    @ApiModelProperty(value = "解封时间")
    private LocalDateTime unbanTime;

    @ApiModelProperty(value = "解封管理员 ID", example = "9")
    private Long unbanAdminId;

    @ApiModelProperty(value = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @ApiModelProperty(value = "注册时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
