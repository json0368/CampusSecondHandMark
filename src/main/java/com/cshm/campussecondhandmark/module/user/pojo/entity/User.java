package com.cshm.campussecondhandmark.module.user.pojo.entity;

import com.cshm.campussecondhandmark.common.entity.BaseEntity;
import com.cshm.campussecondhandmark.module.user.enums.CampusVerifyStatusEnum;
import com.cshm.campussecondhandmark.module.user.enums.UserRoleEnum;
import com.cshm.campussecondhandmark.module.user.enums.UserStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "用户实体类")
public class User extends BaseEntity {

    @ApiModelProperty(value = "用户姓名")
    private String name;

    @ApiModelProperty(value = "用户密码")
    private String password;

    @ApiModelProperty(value = "用户昵称")
    private String nickname;

    @ApiModelProperty(value = "用户邮箱")
    private String email;

    @ApiModelProperty(value = "用户手机号")
    private String phone;

    @ApiModelProperty(value = "用户学号")
    private String student_no;

    @ApiModelProperty(value = "用户专业")
    private String major;

    @ApiModelProperty(value = "用户头像 URL")
    private String avatar_url;

    @ApiModelProperty(value = "用户角色")
    private UserRoleEnum role;

    @ApiModelProperty(value = "用户账号状态")
    private UserStatusEnum status;

    @ApiModelProperty(value = "用户校园认证状态")
    private CampusVerifyStatusEnum verify_status;

    @ApiModelProperty(value = "用户最后一次登录时间")
    private LocalDateTime last_login_time;
}
