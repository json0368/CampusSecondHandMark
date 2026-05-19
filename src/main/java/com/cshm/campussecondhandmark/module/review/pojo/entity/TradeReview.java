package com.cshm.campussecondhandmark.module.review.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("trade_review")
@ApiModel(description = "交易评价")
public class TradeReview {

    @ApiModelProperty(value = "评价 ID")
    private Long id;

    @ApiModelProperty(value = "订单 ID")
    private Long orderId;

    @ApiModelProperty(value = "评价人用户 ID")
    private Long reviewerId;

    @ApiModelProperty(value = "被评价人用户 ID")
    private Long revieweeId;

    @ApiModelProperty(value = "评分，1-5 分")
    private Integer score;

    @ApiModelProperty(value = "评价内容")
    private String content;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
