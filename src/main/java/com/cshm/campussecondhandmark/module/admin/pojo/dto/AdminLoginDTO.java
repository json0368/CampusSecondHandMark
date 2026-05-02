package com.cshm.campussecondhandmark.module.admin.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "管理员登录参数")
public class AdminLoginDTO {

    @ApiModelProperty(value = "邮箱", required = true, example = "admin@example.com")
    private String email;

    @ApiModelProperty(value = "密码", required = true, example = "password123")
    private String password;
}
