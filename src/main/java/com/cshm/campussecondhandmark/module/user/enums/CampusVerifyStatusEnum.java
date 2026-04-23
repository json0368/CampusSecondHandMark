package com.cshm.campussecondhandmark.module.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CampusVerifyStatusEnum {

    PENDING(0, "待审核"),
    APPROVED(1, "已认证"),
    REJECTED(2, "未通过"),
    NOT_APPLICABLE(9, "不适用");   // 管理员账号不需要校园认证

    @EnumValue
    private final Integer code;

    private final String desc;
}
