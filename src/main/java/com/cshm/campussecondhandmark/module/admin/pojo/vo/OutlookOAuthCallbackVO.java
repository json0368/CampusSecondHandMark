package com.cshm.campussecondhandmark.module.admin.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Outlook OAuth 回调结果")
public class OutlookOAuthCallbackVO {

    @ApiModelProperty(value = "刷新令牌")
    private String refreshToken;

    @ApiModelProperty(value = "提示信息")
    private String message;
}
