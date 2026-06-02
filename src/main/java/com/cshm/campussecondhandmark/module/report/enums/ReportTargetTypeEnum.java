package com.cshm.campussecondhandmark.module.report.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportTargetTypeEnum {

    PRODUCT("PRODUCT", "商品"),
    USER("USER", "用户"),
    REVIEW("REVIEW", "评价");

    @EnumValue
    private final String code;

    private final String desc;
}
