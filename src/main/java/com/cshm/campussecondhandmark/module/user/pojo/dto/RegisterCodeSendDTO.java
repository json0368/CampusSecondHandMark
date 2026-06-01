package com.cshm.campussecondhandmark.module.user.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "注册邮箱验证码发送参数")
public class RegisterCodeSendDTO {

    @ApiModelProperty(value = "邮箱", required = true, example = "zhangsan@example.com")
    private String email;
}
