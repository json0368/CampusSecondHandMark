package com.cshm.campussecondhandmark.module.chat.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Matrix 聊天凭证")
public class ChatCredentialsVO {

    @ApiModelProperty(value = "Matrix Access Token")
    private String accessToken;

    @ApiModelProperty(value = "Matrix 用户 ID，如 @u_123:localhost")
    private String userId;

    @ApiModelProperty(value = "设备 ID")
    private String deviceId;

    @ApiModelProperty(value = "Matrix Homeserver URL")
    private String homeserverUrl;
}
