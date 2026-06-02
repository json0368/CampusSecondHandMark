package com.cshm.campussecondhandmark.module.product.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "商品会话开通请求")
public class ChatConversationOpenRequest {

    @ApiModelProperty(value = "业务唯一键")
    private String businessKey;

    @ApiModelProperty(value = "买家用户 ID")
    private Long buyerId;

    @ApiModelProperty(value = "卖家用户 ID")
    private Long sellerId;

    @ApiModelProperty(value = "商品 ID")
    private Long productId;

    @ApiModelProperty(value = "商品标题")
    private String productTitle;

    @ApiModelProperty(value = "商品封面 URL")
    private String productCoverUrl;

    @ApiModelProperty(value = "买家资料快照")
    private ChatUserProfileDTO buyerProfile;

    @ApiModelProperty(value = "卖家资料快照")
    private ChatUserProfileDTO sellerProfile;
}
