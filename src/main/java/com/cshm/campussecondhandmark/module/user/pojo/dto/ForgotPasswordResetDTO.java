package com.cshm.campussecondhandmark.module.user.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "找回密码重置参数")
public class ForgotPasswordResetDTO {

    @ApiModelProperty(value = "邮箱", required = true, example = "zhangsan@example.com")
    private String email;

    @ApiModelProperty(value = "验证码", required = true, example = "123456")
    private String code;

    @ApiModelProperty(value = "新密码", required = true, example = "654321")
    private String newPassword;

    @ApiModelProperty(value = "确认新密码", required = true, example = "654321")
    private String confirmPassword;
}
