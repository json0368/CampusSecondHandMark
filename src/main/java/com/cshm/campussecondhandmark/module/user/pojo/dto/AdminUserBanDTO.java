package com.cshm.campussecondhandmark.module.user.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "后台用户封禁参数")
public class AdminUserBanDTO {

    @ApiModelProperty(value = "封禁原因", required = true, example = "发布违规商品")
    private String reason;
}
