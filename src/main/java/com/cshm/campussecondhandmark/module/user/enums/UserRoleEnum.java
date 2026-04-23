package com.cshm.campussecondhandmark.module.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRoleEnum {

    USER(0, "普通用户"),
    ADMIN(1, "管理员");

    @EnumValue
    private final Integer code;

    private final String desc;
}
