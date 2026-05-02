package com.cshm.campussecondhandmark.module.product.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductSaleStatusEnum {

    DRAFT(0, "草稿"),
    ON_SHELF(1, "上架中"),
    IN_TRANSACTION(2, "交易中"),
    SOLD(3, "已售出"),
    OFF_SHELF(4, "主动下架"),
    REMOVED_BY_ADMIN(5, "管理员下架");

    @EnumValue
    private final Integer code;

    private final String desc;
}
