package com.cshm.campussecondhandmark.module.order.pojo.vo;

import com.cshm.campussecondhandmark.module.order.enums.TradeOrderStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel(description = "订单详情信息")
public class OrderDetailVO {

    @ApiModelProperty(value = "订单 ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "订单号", example = "TO202605051200001234")
    private String orderNo;

    @ApiModelProperty(value = "商品 ID", example = "10")
    private Long productId;

    @ApiModelProperty(value = "商品标题", example = "九成新高等数学教材")
    private String productTitle;

    @ApiModelProperty(value = "商品封面图", example = "http://localhost:8086/upload/product-cover.jpg")
    private String productCoverImageUrl;

    @ApiModelProperty(value = "买家 ID", example = "2")
    private Long buyerId;

    @ApiModelProperty(value = "买家昵称", example = "张三")
    private String buyerNickname;

    @ApiModelProperty(value = "卖家 ID", example = "3")
    private Long sellerId;

    @ApiModelProperty(value = "卖家昵称", example = "李四")
    private String sellerNickname;

    @ApiModelProperty(value = "订单金额", example = "25.00")
    private BigDecimal orderAmount;

    @ApiModelProperty(value = "订单状态", example = "IN_TRANSACTION")
    private TradeOrderStatusEnum status;

    @ApiModelProperty(value = "取消原因", example = "双方临时调整交易时间")
    private String cancelReason;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "确认时间")
    private LocalDateTime confirmedTime;

    @ApiModelProperty(value = "完成时间")
    private LocalDateTime completedTime;

    @ApiModelProperty(value = "当前用户是否可确认", example = "false")
    private Boolean canConfirm;

    @ApiModelProperty(value = "当前用户是否可取消", example = "true")
    private Boolean canCancel;

    @ApiModelProperty(value = "当前用户是否可完成", example = "true")
    private Boolean canComplete;

    @ApiModelProperty(value = "当前用户是否可评价", example = "false")
    private Boolean canReview;
}
