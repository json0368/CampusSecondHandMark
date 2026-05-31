package com.cshm.campussecondhandmark.module.user.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cshm.campussecondhandmark.module.user.enums.CampusVerifyStatusEnum;
import com.cshm.campussecondhandmark.module.user.enums.UserRoleEnum;
import com.cshm.campussecondhandmark.module.user.enums.UserStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("platform_user")
@ApiModel(description = "用户实体类")
public class User {

    @ApiModelProperty(value = "用户 ID")
    Long id;

    @ApiModelProperty(value = "用户姓名")
    private String username;

    @ApiModelProperty(value = "用户密码")
    private String passwordHash;

    @ApiModelProperty(value = "用户昵称")
    private String nickname;

    @ApiModelProperty(value = "用户邮箱")
    private String email;

    @ApiModelProperty(value = "用户手机号")
    private String phone;

    @ApiModelProperty(value = "用户学号")
    private String studentNo;

    @ApiModelProperty(value = "用户专业")
    private String major;

    @ApiModelProperty(value = "用户头像 URL")
    private String avatarUrl;

    @ApiModelProperty(value = "用户角色")
    private UserRoleEnum role;

    @ApiModelProperty(value = "用户账号状态")
    private UserStatusEnum status;

    @ApiModelProperty(value = "用户校园认证状态")
    private CampusVerifyStatusEnum campusVerifyStatus;

    @ApiModelProperty(value = "校园认证审核备注")
    private String campusVerifyRemark;

    @ApiModelProperty(value = "校园认证审核时间")
    private LocalDateTime campusVerifyTime;

    @ApiModelProperty(value = "校园认证审核管理员ID")
    private Long campusVerifyAdminId;

    @ApiModelProperty(value = "封禁原因")
    private String banReason;

    @ApiModelProperty(value = "封禁时间")
    private LocalDateTime banTime;

    @ApiModelProperty(value = "封禁管理员ID")
    private Long banAdminId;

    @ApiModelProperty(value = "解封原因")
    private String unbanReason;

    @ApiModelProperty(value = "解封时间")
    private LocalDateTime unbanTime;

    @ApiModelProperty(value = "解封管理员ID")
    private Long unbanAdminId;

    @ApiModelProperty(value = "用户最后一次登录时间")
    private LocalDateTime lastLoginTime;

    @ApiModelProperty(value = "用户创建时间")
    LocalDateTime createTime;

    @ApiModelProperty(value = "用户更新时间")
    LocalDateTime updateTime;
}
