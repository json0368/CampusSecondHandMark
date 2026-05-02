package com.cshm.campussecondhandmark.module.admin.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "管理员登录结果")
public class AdminLoginVO {

    @ApiModelProperty(value = "管理员 ID")
    private Long id;

    @ApiModelProperty(value = "管理员昵称")
    private String nickname;

    @ApiModelProperty(value = "JWT 令牌")
    private String token;
}
