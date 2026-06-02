package com.cshm.campussecondhandmark.module.product.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "商品会话用户资料")
public class ChatUserProfileDTO {

    @ApiModelProperty(value = "用户昵称")
    private String nickname;

    @ApiModelProperty(value = "用户头像 URL")
    private String avatarUrl;
}
