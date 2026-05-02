package com.cshm.campussecondhandmark.module.user.pojo.vo;

import com.cshm.campussecondhandmark.module.user.enums.CampusVerifyStatusEnum;
import com.cshm.campussecondhandmark.module.user.enums.UserRoleEnum;
import com.cshm.campussecondhandmark.module.user.enums.UserStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "当前登录用户信息")
public class CurrentUserVO {

    @ApiModelProperty(value = "用户 ID")
    private Long id;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "手机号")
    private String phone;

    @ApiModelProperty(value = "学号")
    private String studentNo;

    @ApiModelProperty(value = "专业")
    private String major;

    @ApiModelProperty(value = "头像 URL")
    private String avatarUrl;

    @ApiModelProperty(value = "角色")
    private UserRoleEnum role;

    @ApiModelProperty(value = "账号状态")
    private UserStatusEnum status;

    @ApiModelProperty(value = "校园认证状态")
    private CampusVerifyStatusEnum campusVerifyStatus;

    @ApiModelProperty(value = "最后登录时间")
    private LocalDateTime lastLoginTime;
}
