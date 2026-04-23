package com.cshm.campussecondhandmark.module.user.service;

import com.cshm.campussecondhandmark.common.result.PageResult;
import com.cshm.campussecondhandmark.module.admin.pojo.dto.AdminLoginDTO;
import com.cshm.campussecondhandmark.module.admin.pojo.vo.AdminLoginVO;
import com.cshm.campussecondhandmark.module.user.pojo.dto.UserLoginDTO;
import com.cshm.campussecondhandmark.module.user.pojo.dto.UserProfileUpdateDTO;
import com.cshm.campussecondhandmark.module.user.pojo.dto.UserRegisterDTO;
import com.cshm.campussecondhandmark.module.user.pojo.dto.UserUpdateDTO;
import com.cshm.campussecondhandmark.module.user.pojo.entity.User;
import com.cshm.campussecondhandmark.module.user.pojo.vo.CurrentUserVO;
import com.cshm.campussecondhandmark.module.user.pojo.vo.UserLoginVO;
import com.cshm.campussecondhandmark.module.user.pojo.vo.UserProfileVO;

public interface UserService {

    UserLoginVO login(UserLoginDTO userLoginDTO);

    UserLoginVO register(UserRegisterDTO userRegisterDTO);

    AdminLoginVO adminLogin(AdminLoginDTO adminLoginDTO);

    User get(Long id);

    CurrentUserVO getCurrentUser(Long currentUserId);

    UserProfileVO getUserProfile(Long userId);

    PageResult page(Integer pageNum, Integer pageSize);

    void update(UserUpdateDTO userUpdateDTO);

    void updateProfile(Long currentUserId, UserProfileUpdateDTO userProfileUpdateDTO);
}
