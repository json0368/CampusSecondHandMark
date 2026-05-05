package com.cshm.campussecondhandmark.module.order.pojo.dto;

import com.cshm.campussecondhandmark.module.order.enums.TradeOrderStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "我的订单列表查询参数")
public class OrderQueryDTO {

    @ApiModelProperty(value = "订单状态筛选", example = "PENDING_CONFIRM")
    private TradeOrderStatusEnum status;

    @ApiModelProperty(value = "身份视角，BUYER / SELLER / ALL", example = "ALL")
    private String identityType;

    @ApiModelProperty(value = "页码，从 1 开始", example = "1")
    private Integer pageNum;

    @ApiModelProperty(value = "每页条数", example = "10")
    private Integer pageSize;
}
