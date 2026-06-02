package com.cshm.campussecondhandmark.module.report.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportHandleResultEnum {

    VALID("VALID", "有效"),
    INVALID("INVALID", "无效"),
    DUPLICATE("DUPLICATE", "重复举报");

    @EnumValue
    private final String code;

    private final String desc;
}
