package com.cshm.campussecondhandmark.module.order.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cshm.campussecondhandmark.module.order.enums.TradeOrderStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("trade_order")
@ApiModel(description = "订单实体")
public class TradeOrder {

    @ApiModelProperty(value = "订单 ID")
    private Long id;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "商品 ID")
    private Long productId;

    @ApiModelProperty(value = "买家用户 ID")
    private Long buyerId;

    @ApiModelProperty(value = "卖家用户 ID")
    private Long sellerId;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal orderAmount;

    @ApiModelProperty(value = "订单状态")
    private TradeOrderStatusEnum status;

    @ApiModelProperty(value = "取消原因")
    private String cancelReason;

    @ApiModelProperty(value = "确认时间")
    private LocalDateTime confirmedTime;

    @ApiModelProperty(value = "完成时间")
    private LocalDateTime completedTime;

    @ApiModelProperty(value = "取消时间")
    private LocalDateTime cancelledTime;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
