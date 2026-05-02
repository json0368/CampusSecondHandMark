package com.cshm.campussecondhandmark.module.product.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.cshm.campussecondhandmark.common.exception.BaseException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductConditionEnum {

    UNKNOWN(0, "未设置"),
    LIKE_NEW(1, "几乎全新"),
    GOOD(2, "轻微使用"),
    FAIR(3, "使用痕迹明显");

    @EnumValue
    private final Integer code;

    private final String desc;

    public static ProductConditionEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProductConditionEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new BaseException("商品成色不正确");
    }
}
