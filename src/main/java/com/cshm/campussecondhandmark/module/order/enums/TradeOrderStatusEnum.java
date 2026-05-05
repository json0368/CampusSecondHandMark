package com.cshm.campussecondhandmark.module.order.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TradeOrderStatusEnum {

    PENDING_CONFIRM(0, "待确认"),
    IN_TRANSACTION(1, "交易中"),
    COMPLETED(2, "已完成"),
    CANCELLED(3, "已取消");

    @EnumValue
    private final Integer code;

    private final String desc;
}
