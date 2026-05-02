package com.cshm.campussecondhandmark.module.product.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CategoryStatusEnum {

    ENABLED(0, "启用"),
    DISABLED(1, "停用");

    @EnumValue
    private final Integer code;

    private final String desc;
}
