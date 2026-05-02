package com.cshm.campussecondhandmark.module.user.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "用户登录参数")
public class UserLoginDTO {

    @ApiModelProperty(value = "登录邮箱", required = true, example = "zhangsan@example.com")
    private String email;

    @ApiModelProperty(value = "登录密码", required = true, example = "password123")
    private String password;
}
