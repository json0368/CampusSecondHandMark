package com.cshm.campussecondhandmark.module.product.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "商品会话开通结果")
public class ProductConversationOpenVO {

    @ApiModelProperty(value = "买家用户 ID")
    private Long buyerId;

    @ApiModelProperty(value = "买家昵称")
    private String buyerNickname;

    @ApiModelProperty(value = "买家头像 URL")
    private String buyerAvatarUrl;

    @ApiModelProperty(value = "卖家用户 ID")
    private Long sellerId;

    @ApiModelProperty(value = "卖家昵称")
    private String sellerNickname;

    @ApiModelProperty(value = "卖家头像 URL")
    private String sellerAvatarUrl;

    @ApiModelProperty(value = "商品 ID")
    private Long productId;

    @ApiModelProperty(value = "商品标题")
    private String productTitle;

    @ApiModelProperty(value = "商品封面 URL")
    private String productCoverUrl;
}
