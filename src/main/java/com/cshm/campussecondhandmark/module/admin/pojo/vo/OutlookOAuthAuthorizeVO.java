package com.cshm.campussecondhandmark.module.admin.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Outlook OAuth 授权地址返回结果")
public class OutlookOAuthAuthorizeVO {

    @ApiModelProperty(value = "微软授权地址")
    private String authorizeUrl;

    @ApiModelProperty(value = "授权状态有效期，单位秒")
    private Integer expireSeconds;
}
