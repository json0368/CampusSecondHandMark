package com.cshm.campussecondhandmark.module.product.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "商品会话开通结果")
public class ProductConversationOpenVO {

    @ApiModelProperty(value = "会话 ID")
    private String conversationId;

    @ApiModelProperty(value = "Matrix 房间 ID")
    private String matrixRoomId;

    @ApiModelProperty(value = "聊天短期凭证")
    private String chatTicket;

    @ApiModelProperty(value = "凭证有效秒数")
    private Integer ticketExpireSeconds;
}
