package com.cshm.campussecondhandmark.module.user.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "User login DTO")
public class UserLoginDTO {

    @ApiModelProperty(value = "email", required = true, example = "xxx@gmail.com")
    private String email;

    @ApiModelProperty(value = "password", required = true, example = "password123")
    private String password;
}
