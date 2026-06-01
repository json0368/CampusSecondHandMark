package com.cshm.campussecondhandmark.module.user.service;

import com.cshm.campussecondhandmark.common.result.PageResult;
import com.cshm.campussecondhandmark.module.admin.pojo.dto.AdminLoginDTO;
import com.cshm.campussecondhandmark.module.admin.pojo.vo.AdminLoginVO;
import com.cshm.campussecondhandmark.module.user.pojo.dto.AdminUserBanDTO;
import com.cshm.campussecondhandmark.module.user.pojo.dto.AdminUserQueryDTO;
import com.cshm.campussecondhandmark.module.user.pojo.dto.AdminUserUnbanDTO;
import com.cshm.campussecondhandmark.module.user.pojo.dto.CampusVerifyAuditDTO;
import com.cshm.campussecondhandmark.module.user.pojo.dto.CampusVerifyQueryDTO;
import com.cshm.campussecondhandmark.module.user.pojo.dto.ForgotPasswordCodeSendDTO;
import com.cshm.campussecondhandmark.module.user.pojo.dto.ForgotPasswordResetDTO;
import com.cshm.campussecondhandmark.module.user.pojo.dto.RegisterCodeSendDTO;
import com.cshm.campussecondhandmark.module.user.pojo.dto.UserChangePasswordDTO;
import com.cshm.campussecondhandmark.module.user.pojo.dto.UserLoginDTO;
import com.cshm.campussecondhandmark.module.user.pojo.dto.UserProfileUpdateDTO;
import com.cshm.campussecondhandmark.module.user.pojo.dto.UserRegisterDTO;
import com.cshm.campussecondhandmark.module.user.pojo.dto.UserUpdateDTO;
import com.cshm.campussecondhandmark.module.user.pojo.entity.User;
import com.cshm.campussecondhandmark.module.user.pojo.vo.AdminUserDetailVO;
import com.cshm.campussecondhandmark.module.user.pojo.vo.AdminUserListVO;
import com.cshm.campussecondhandmark.module.user.pojo.vo.CampusVerifyAuditVO;
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

    PageResult<AdminUserListVO> pageAdminUsers(AdminUserQueryDTO dto);

    AdminUserDetailVO getAdminUserDetail(Long userId);

    PageResult<CampusVerifyAuditVO> pageCampusVerifyUsers(CampusVerifyQueryDTO dto);

    void update(UserUpdateDTO userUpdateDTO);

    void banUser(Long userId, Long adminId, AdminUserBanDTO dto);

    void unbanUser(Long userId, Long adminId, AdminUserUnbanDTO dto);

    void auditCampusVerify(Long userId, Long adminId, CampusVerifyAuditDTO dto);

    void updateProfile(Long currentUserId, UserProfileUpdateDTO userProfileUpdateDTO);

    void changePassword(Long currentUserId, UserChangePasswordDTO dto);

    void sendRegisterCode(RegisterCodeSendDTO dto);

    void sendForgotPasswordCode(ForgotPasswordCodeSendDTO dto);

    void resetForgotPassword(ForgotPasswordResetDTO dto);
}
