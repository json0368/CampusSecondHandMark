package com.cshm.campussecondhandmark.module.chat.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "创建聊天房间请求")
public class ChatRoomCreateDTO {

    @ApiModelProperty(value = "卖家用户 ID", required = true, example = "456")
    private Long sellerId;

    @ApiModelProperty(value = "商品标题", required = true, example = "二手 MacBook Pro")
    private String productTitle;

    @ApiModelProperty(value = "房间主题描述（可选）")
    private String topic;
}
