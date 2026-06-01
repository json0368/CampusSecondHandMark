package com.cshm.campussecondhandmark.module.user.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "用户注册参数")
public class UserRegisterDTO {

    @ApiModelProperty(value = "用户名", required = true, example = "zhangsan")
    private String username;

    @ApiModelProperty(value = "昵称", required = true, example = "张三")
    private String nickname;

    @ApiModelProperty(value = "邮箱", required = true, example = "zhangsan@example.com")
    private String email;

    @ApiModelProperty(value = "手机号", example = "13800000000")
    private String phone;

    @ApiModelProperty(value = "学号", required = true, example = "20240001")
    private String studentNo;

    @ApiModelProperty(value = "专业", required = true, example = "计算机科学与技术")
    private String major;

    @ApiModelProperty(value = "密码", required = true, example = "123456")
    private String password;

    @ApiModelProperty(value = "邮箱验证码", required = true, example = "123456")
    private String emailCode;
}
