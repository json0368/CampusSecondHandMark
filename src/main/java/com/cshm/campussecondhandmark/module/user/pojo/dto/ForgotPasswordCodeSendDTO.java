package com.cshm.campussecondhandmark.module.user.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "找回密码验证码发送参数")
public class ForgotPasswordCodeSendDTO {

    @ApiModelProperty(value = "邮箱", required = true, example = "zhangsan@example.com")
    private String email;
}
