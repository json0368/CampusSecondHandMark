package com.cshm.campussecondhandmark.module.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatusEnum {

    NORMAL(0, "正常"),
    BANNED(1, "封禁");

    @EnumValue
    private final Integer code;

    private final String desc;
}