package com.cshm.campussecondhandmark.module.user.service.support;

import com.cshm.campussecondhandmark.common.exception.BaseException;
import com.cshm.campussecondhandmark.module.user.enums.UserRoleEnum;
import com.cshm.campussecondhandmark.module.user.enums.UserStatusEnum;
import com.cshm.campussecondhandmark.module.user.mapper.UserMapper;
import com.cshm.campussecondhandmark.module.user.pojo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserAccessValidator {

    @Autowired
    private UserMapper userMapper;

    public User getNormalUserOrThrow(Long userId) {
        return getNormalUserOrThrow(userId, "当前账号不是普通用户", "账号已被封禁");
    }

    public User getNormalUserOrThrow(Long userId, String roleMessage, String bannedMessage) {
        if (userId == null) {
            throw new BaseException("用户未登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BaseException("用户不存在");
        }
        if (user.getRole() != UserRoleEnum.USER) {
            throw new BaseException(roleMessage);
        }
        if (user.getStatus() == UserStatusEnum.BANNED) {
            throw new BaseException(bannedMessage);
        }
        return user;
    }
}
