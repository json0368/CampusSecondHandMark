package com.cshm.campussecondhandmark.module.review.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "交易评价 VO")
public class ReviewVO {

    @ApiModelProperty(value = "评价 ID", example = "100")
    private Long id;

    @ApiModelProperty(value = "订单 ID", example = "1")
    private Long orderId;

    @ApiModelProperty(value = "商品 ID", example = "30")
    private Long productId;

    @ApiModelProperty(value = "商品标题", example = "book")
    private String productTitle;

    @ApiModelProperty(value = "商品 url", example = "http://example.com/product-cover.jpg")
    private String productCoverImageUrl;

    @ApiModelProperty(value = "评价人用户 ID", example = "10")
    private Long reviewerId;

    @ApiModelProperty(value = "评价人昵称", example = "buyer")
    private String reviewerNickname;

    @ApiModelProperty(value = "评价人头像 URL")
    private String reviewerAvatarUrl;

    @ApiModelProperty(value = "被评价人用户 ID", example = "20")
    private Long revieweeId;

    @ApiModelProperty(value = "被评价人昵称", example = "seller")
    private String revieweeNickname;

    @ApiModelProperty(value = "被评价人头像 URL")
    private String revieweeAvatarUrl;

    @ApiModelProperty(value = "评分，1-5 分", example = "5")
    private Integer score;

    @ApiModelProperty(value = "评价内容", example = "smooth trade")
    private String content;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
