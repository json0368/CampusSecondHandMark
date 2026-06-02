package com.cshm.campussecondhandmark.module.report.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportStatusEnum {

    PENDING("PENDING", "待处理"),
    HANDLED("HANDLED", "已处理");

    @EnumValue
    private final String code;

    private final String desc;
}
