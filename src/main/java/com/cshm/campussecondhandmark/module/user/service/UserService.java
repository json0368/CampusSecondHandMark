package com.cshm.campussecondhandmark.module.user.service;

import com.cshm.campussecondhandmark.module.user.pojo.dto.UserLoginDTO;
import com.cshm.campussecondhandmark.module.user.pojo.entity.User;

public interface UserService {
    User login(UserLoginDTO userLoginDTO);
}
