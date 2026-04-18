package com.cshm.campussecondhandmark.module.user.pojo.dto;

import io.swagger.annotations.ApiModelProperty;

public class UserRegisterDTO {

    @ApiModelProperty(value = "用户邮箱", required = true, example = "xxx@gmail.com")
    private String email;

    @ApiModelProperty(value = "用户手机号")
    private String phone;

    @ApiModelProperty(value = "用户密码", required = true, example = "password123")
    private String password;
}
