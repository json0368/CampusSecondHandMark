package com.cshm.campussecondhandmark.module.user.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "后台用户解封参数")
public class AdminUserUnbanDTO {

    @ApiModelProperty(value = "解封原因", required = true, example = "已完成整改")
    private String reason;
}
