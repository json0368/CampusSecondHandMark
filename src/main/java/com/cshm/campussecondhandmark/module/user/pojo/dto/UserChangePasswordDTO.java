package com.cshm.campussecondhandmark.module.user.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "用户修改密码参数")
public class UserChangePasswordDTO {

    @ApiModelProperty(value = "旧密码", required = true, example = "123456")
    private String oldPassword;

    @ApiModelProperty(value = "新密码", required = true, example = "654321")
    private String newPassword;

    @ApiModelProperty(value = "确认新密码", required = true, example = "654321")
    private String confirmPassword;
}
