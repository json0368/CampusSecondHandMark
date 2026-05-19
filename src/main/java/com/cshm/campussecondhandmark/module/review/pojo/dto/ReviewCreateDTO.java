package com.cshm.campussecondhandmark.module.review.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "创建订单评价")
public class ReviewCreateDTO {

    @ApiModelProperty(value = "订单ID", required = true, example = "1")
    private Long orderId;

    @ApiModelProperty(value = "分数，从1到5", required = true, example = "5")
    private Integer score;

    @ApiModelProperty(value = "评价内容", example = "很好的商品，卖家很负责！")
    private String content;
}
