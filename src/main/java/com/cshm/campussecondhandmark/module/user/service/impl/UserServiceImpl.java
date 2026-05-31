package com.cshm.campussecondhandmark.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cshm.campussecondhandmark.common.exception.BaseException;
import com.cshm.campussecondhandmark.common.properties.JwtProperties;
import com.cshm.campussecondhandmark.common.result.PageResult;
import com.cshm.campussecondhandmark.common.utils.JwtUtil;
import com.cshm.campussecondhandmark.module.admin.pojo.dto.AdminLoginDTO;
import com.cshm.campussecondhandmark.module.admin.pojo.vo.AdminLoginVO;
import com.cshm.campussecondhandmark.module.user.pojo.dto.AdminUserBanDTO;
import com.cshm.campussecondhandmark.module.user.pojo.dto.AdminUserQueryDTO;
import com.cshm.campussecondhandmark.module.user.pojo.dto.AdminUserUnbanDTO;
import com.cshm.campussecondhandmark.module.user.enums.CampusVerifyStatusEnum;
import com.cshm.campussecondhandmark.module.user.enums.UserRoleEnum;
import com.cshm.campussecondhandmark.module.user.enums.UserStatusEnum;
import com.cshm.campussecondhandmark.module.user.mapper.UserMapper;
import com.cshm.campussecondhandmark.module.user.pojo.dto.CampusVerifyAuditDTO;
import com.cshm.campussecondhandmark.module.user.pojo.dto.CampusVerifyQueryDTO;
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
import com.cshm.campussecondhandmark.module.user.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    @Transactional
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        User user = validateLogin(userLoginDTO);
        if (user.getRole() != UserRoleEnum.USER) {
            throw new BaseException("管理员账号请使用后台登录");
        }

        refreshLastLoginTime(user);
        return buildUserLoginVO(user);
    }

    @Override
    @Transactional
    public UserLoginVO register(UserRegisterDTO userRegisterDTO) {
        String username = trimToNull(userRegisterDTO.getUsername());
        String nickname = trimToNull(userRegisterDTO.getNickname());
        String email = trimToNull(userRegisterDTO.getEmail());
        String phone = trimToNull(userRegisterDTO.getPhone());
        String studentNo = trimToNull(userRegisterDTO.getStudentNo());
        String major = trimToNull(userRegisterDTO.getMajor());
        String password = trimToNull(userRegisterDTO.getPassword());

        if (!StringUtils.hasText(username)) {
            throw new BaseException("用户名不能为空");
        }
        if (!StringUtils.hasText(nickname)) {
            throw new BaseException("昵称不能为空");
        }
        if (!StringUtils.hasText(email)) {
            throw new BaseException("邮箱不能为空");
        }
        if (!StringUtils.hasText(studentNo)) {
            throw new BaseException("学号不能为空");
        }
        if (!StringUtils.hasText(major)) {
            throw new BaseException("专业不能为空");
        }
        if (!StringUtils.hasText(password)) {
            throw new BaseException("密码不能为空");
        }

        if (exists(User::getUsername, username)) {
            throw new BaseException("用户名已被注册");
        }
        if (exists(User::getEmail, email)) {
            throw new BaseException("邮箱已被注册");
        }
        if (StringUtils.hasText(phone) && exists(User::getPhone, phone)) {
            throw new BaseException("手机号已被注册");
        }
        if (exists(User::getStudentNo, studentNo)) {
            throw new BaseException("学号已被注册");
        }

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername(username);
        user.setNickname(nickname);
        user.setEmail(email);
        user.setPhone(phone);
        user.setStudentNo(studentNo);
        user.setMajor(major);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(UserRoleEnum.USER);
        user.setStatus(UserStatusEnum.NORMAL);
        user.setCampusVerifyStatus(CampusVerifyStatusEnum.PENDING);
        user.setLastLoginTime(now);
        user.setCreateTime(now);
        user.setUpdateTime(now);

        if (!save(user)) {
            throw new BaseException("注册失败");
        }
        return buildUserLoginVO(user);
    }

    @Override
    @Transactional
    public AdminLoginVO adminLogin(AdminLoginDTO adminLoginDTO) {
        User user = validateAdminLogin(adminLoginDTO);
        refreshLastLoginTime(user);
        return buildAdminLoginVO(user);
    }

    @Override
    public User get(Long id) {
        return getUserOrThrow(id);
    }

    @Override
    public CurrentUserVO getCurrentUser(Long currentUserId) {
        return buildCurrentUserVO(getUserOrThrow(currentUserId));
    }

    @Override
    public UserProfileVO getUserProfile(Long userId) {
        return buildUserProfileVO(getUserOrThrow(userId));
    }

    @Override
    public PageResult page(Integer pageNum, Integer pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getRole, UserRoleEnum.USER);
        Page<User> resultPage = page(page, queryWrapper);
        return new PageResult(resultPage.getTotal(), resultPage.getRecords());
    }

    @Override
    public PageResult<AdminUserListVO> pageAdminUsers(AdminUserQueryDTO dto) {
        AdminUserQueryDTO queryDTO = dto == null ? new AdminUserQueryDTO() : dto;
        Page<User> page = new Page<>(normalizePageNum(queryDTO.getPageNum()), normalizePageSize(queryDTO.getPageSize()));
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getRole, UserRoleEnum.USER);

        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(User::getStatus, queryDTO.getStatus());
        }

        String keyword = trimToNull(queryDTO.getKeyword());
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getNickname, keyword)
                    .or()
                    .like(User::getEmail, keyword)
                    .or()
                    .like(User::getStudentNo, keyword));
        }
        queryWrapper.orderByDesc(User::getCreateTime).orderByDesc(User::getId);

        Page<User> resultPage = page(page, queryWrapper);
        List<AdminUserListVO> records = resultPage.getRecords().stream()
                .map(this::buildAdminUserListVO)
                .collect(Collectors.toList());
        return new PageResult<>(resultPage.getTotal(), records);
    }

    @Override
    public AdminUserDetailVO getAdminUserDetail(Long userId) {
        User user = getUserOrThrow(userId);
        validateUserRole(user, "查看目标必须是普通用户");
        return buildAdminUserDetailVO(user);
    }

    @Override
    public PageResult<CampusVerifyAuditVO> pageCampusVerifyUsers(CampusVerifyQueryDTO dto) {
        CampusVerifyQueryDTO queryDTO = dto == null ? new CampusVerifyQueryDTO() : dto;
        Page<User> page = new Page<>(normalizePageNum(queryDTO.getPageNum()), normalizePageSize(queryDTO.getPageSize()));
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getRole, UserRoleEnum.USER)
                .eq(User::getCampusVerifyStatus,
                        queryDTO.getCampusVerifyStatus() == null ? CampusVerifyStatusEnum.PENDING : queryDTO.getCampusVerifyStatus());

        String keyword = trimToNull(queryDTO.getKeyword());
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getNickname, keyword)
                    .or()
                    .like(User::getStudentNo, keyword));
        }
        queryWrapper.orderByDesc(User::getCreateTime).orderByDesc(User::getId);

        Page<User> resultPage = page(page, queryWrapper);
        List<CampusVerifyAuditVO> records = resultPage.getRecords().stream()
                .map(this::buildCampusVerifyAuditVO)
                .collect(Collectors.toList());
        return new PageResult<>(resultPage.getTotal(), records);
    }

    @Override
    @Transactional
    public void update(UserUpdateDTO userUpdateDTO) {
        if (userUpdateDTO == null) {
            throw new BaseException("更新信息不能为空");
        }

        UserProfileUpdateDTO userProfileUpdateDTO = new UserProfileUpdateDTO();
        userProfileUpdateDTO.setNickname(userUpdateDTO.getNickname());
        userProfileUpdateDTO.setPhone(userUpdateDTO.getPhone());
        userProfileUpdateDTO.setStudentNo(userUpdateDTO.getStudentNo());
        userProfileUpdateDTO.setMajor(userUpdateDTO.getMajor());
        userProfileUpdateDTO.setAvatarUrl(userUpdateDTO.getAvatarUrl());
        updateProfile(userUpdateDTO.getId(), userProfileUpdateDTO);
    }

    @Override
    @Transactional
    public void banUser(Long userId, Long adminId, AdminUserBanDTO dto) {
        if (dto == null) {
            throw new BaseException("封禁信息不能为空");
        }

        requireAdminId(adminId);
        String reason = requireReason(dto.getReason(), "封禁原因不能为空");

        User user = getUserOrThrow(userId);
        validateUserRole(user, "封禁目标必须是普通用户");
        if (user.getStatus() != UserStatusEnum.NORMAL) {
            throw new BaseException("当前用户不是正常状态");
        }

        LocalDateTime now = LocalDateTime.now();
        user.setStatus(UserStatusEnum.BANNED);
        user.setBanReason(reason);
        user.setBanTime(now);
        user.setBanAdminId(adminId);
        clearUnbanAuditFields(user);
        user.setUpdateTime(now);
        if (!updateById(user)) {
            throw new BaseException("封禁失败");
        }
    }

    @Override
    @Transactional
    public void unbanUser(Long userId, Long adminId, AdminUserUnbanDTO dto) {
        if (dto == null) {
            throw new BaseException("解封信息不能为空");
        }

        requireAdminId(adminId);
        String reason = requireReason(dto.getReason(), "解封原因不能为空");

        User user = getUserOrThrow(userId);
        validateUserRole(user, "解封目标必须是普通用户");
        if (user.getStatus() != UserStatusEnum.BANNED) {
            throw new BaseException("当前用户不是封禁状态");
        }

        LocalDateTime now = LocalDateTime.now();
        user.setStatus(UserStatusEnum.NORMAL);
        user.setUnbanReason(reason);
        user.setUnbanTime(now);
        user.setUnbanAdminId(adminId);
        user.setUpdateTime(now);
        if (!updateById(user)) {
            throw new BaseException("解封失败");
        }
    }

    @Override
    @Transactional
    public void auditCampusVerify(Long userId, Long adminId, CampusVerifyAuditDTO dto) {
        if (dto == null) {
            throw new BaseException("审核信息不能为空");
        }
        if (adminId == null) {
            throw new BaseException("管理员未登录");
        }

        CampusVerifyStatusEnum campusVerifyStatus = dto.getCampusVerifyStatus();
        if (campusVerifyStatus != CampusVerifyStatusEnum.APPROVED && campusVerifyStatus != CampusVerifyStatusEnum.REJECTED) {
            throw new BaseException("审核状态非法");
        }

        User user = getUserOrThrow(userId);
        if (user.getRole() != UserRoleEnum.USER) {
            throw new BaseException("审核目标必须是普通用户");
        }
        if (user.getCampusVerifyStatus() != CampusVerifyStatusEnum.PENDING) {
            throw new BaseException("当前用户不是待审核状态");
        }

        String remark = trimToNull(dto.getRemark());
        if (campusVerifyStatus == CampusVerifyStatusEnum.REJECTED && !StringUtils.hasText(remark)) {
            throw new BaseException("驳回原因不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        user.setCampusVerifyStatus(campusVerifyStatus);
        user.setCampusVerifyRemark(remark);
        user.setCampusVerifyAdminId(adminId);
        user.setCampusVerifyTime(now);
        user.setUpdateTime(now);
        if (!updateById(user)) {
            throw new BaseException("校园认证审核失败");
        }
    }

    @Override
    @Transactional
    public void updateProfile(Long currentUserId, UserProfileUpdateDTO userProfileUpdateDTO) {
        if (userProfileUpdateDTO == null) {
            throw new BaseException("更新信息不能为空");
        }

        User user = getUserOrThrow(currentUserId);
        boolean campusInfoChanged = false;

        if (userProfileUpdateDTO.getNickname() != null) {
            String nickname = trimToNull(userProfileUpdateDTO.getNickname());
            if (!StringUtils.hasText(nickname)) {
                throw new BaseException("昵称不能为空");
            }
            user.setNickname(nickname);
        }

        if (userProfileUpdateDTO.getPhone() != null) {
            String phone = trimToNull(userProfileUpdateDTO.getPhone());
            if (StringUtils.hasText(phone) && existsOther(User::getPhone, phone, user.getId())) {
                throw new BaseException("手机号已被注册");
            }
            user.setPhone(phone);
        }

        if (userProfileUpdateDTO.getStudentNo() != null) {
            String studentNo = trimToNull(userProfileUpdateDTO.getStudentNo());
            if (!StringUtils.hasText(studentNo)) {
                throw new BaseException("学号不能为空");
            }
            if (existsOther(User::getStudentNo, studentNo, user.getId())) {
                throw new BaseException("学号已被注册");
            }
            if (!studentNo.equals(user.getStudentNo())) {
                user.setStudentNo(studentNo);
                campusInfoChanged = true;
            }
        }

        if (userProfileUpdateDTO.getMajor() != null) {
            String major = trimToNull(userProfileUpdateDTO.getMajor());
            if (!StringUtils.hasText(major)) {
                throw new BaseException("专业不能为空");
            }
            if (!major.equals(user.getMajor())) {
                user.setMajor(major);
                campusInfoChanged = true;
            }
        }

        if (userProfileUpdateDTO.getAvatarUrl() != null) {
            user.setAvatarUrl(trimToNull(userProfileUpdateDTO.getAvatarUrl()));
        }

        if (campusInfoChanged && user.getRole() == UserRoleEnum.USER) {
            user.setCampusVerifyStatus(CampusVerifyStatusEnum.PENDING);
            clearCampusVerifyAuditFields(user);
        }

        user.setUpdateTime(LocalDateTime.now());
        if (!updateById(user)) {
            throw new BaseException("更新失败");
        }
    }

    private User validateLogin(UserLoginDTO userLoginDTO) {
        String email = trimToNull(userLoginDTO.getEmail());
        String password = userLoginDTO.getPassword();
        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            throw new BaseException("邮箱或密码错误");
        }

        User user = getUserByEmail(email);
        validatePassword(user, password);
        validateNotBanned(user);
        return user;
    }

    private User validateAdminLogin(AdminLoginDTO adminLoginDTO) {
        String email = trimToNull(adminLoginDTO.getEmail());
        String password = adminLoginDTO.getPassword();
        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            throw new BaseException("邮箱或密码错误");
        }

        User user = getUserByEmail(email);
        validatePassword(user, password);
        validateNotBanned(user);
        if (user.getRole() != UserRoleEnum.ADMIN) {
            throw new BaseException("该账号不是管理员");
        }
        return user;
    }

    private User getUserOrThrow(Long userId) {
        if (userId == null) {
            throw new BaseException("用户未登录");
        }

        User user = getById(userId);
        if (user == null) {
            throw new BaseException("用户不存在");
        }
        return user;
    }

    private User getUserByEmail(String email) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, email);
        User user = baseMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BaseException("邮箱或密码错误");
        }
        return user;
    }

    private void validatePassword(User user, String password) {
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BaseException("邮箱或密码错误");
        }
    }

    private void validateNotBanned(User user) {
        if (user.getStatus() == UserStatusEnum.BANNED) {
            throw new BaseException("账号已被封禁");
        }
    }

    private void refreshLastLoginTime(User user) {
        user.setLastLoginTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        updateById(user);
    }

    private <T> boolean exists(SFunction<User, T> column, T value) {
        if (value == null) {
            return false;
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(column, value);
        return count(queryWrapper) > 0;
    }

    private <T> boolean existsOther(SFunction<User, T> column, T value, Long excludeId) {
        if (value == null) {
            return false;
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(column, value);
        if (excludeId != null) {
            queryWrapper.ne(User::getId, excludeId);
        }
        return count(queryWrapper) > 0;
    }

    private UserLoginVO buildUserLoginVO(User user) {
        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(user, userLoginVO);
        userLoginVO.setToken(buildToken(user));
        return userLoginVO;
    }

    private AdminLoginVO buildAdminLoginVO(User user) {
        AdminLoginVO adminLoginVO = new AdminLoginVO();
        adminLoginVO.setId(user.getId());
        adminLoginVO.setNickname(user.getNickname());
        adminLoginVO.setToken(buildToken(user));
        return adminLoginVO;
    }

    private CurrentUserVO buildCurrentUserVO(User user) {
        CurrentUserVO currentUserVO = new CurrentUserVO();
        BeanUtils.copyProperties(user, currentUserVO);
        return currentUserVO;
    }

    private AdminUserListVO buildAdminUserListVO(User user) {
        AdminUserListVO adminUserListVO = new AdminUserListVO();
        BeanUtils.copyProperties(user, adminUserListVO);
        return adminUserListVO;
    }

    private AdminUserDetailVO buildAdminUserDetailVO(User user) {
        AdminUserDetailVO adminUserDetailVO = new AdminUserDetailVO();
        BeanUtils.copyProperties(user, adminUserDetailVO);
        return adminUserDetailVO;
    }

    private CampusVerifyAuditVO buildCampusVerifyAuditVO(User user) {
        CampusVerifyAuditVO campusVerifyAuditVO = new CampusVerifyAuditVO();
        BeanUtils.copyProperties(user, campusVerifyAuditVO);
        return campusVerifyAuditVO;
    }

    private UserProfileVO buildUserProfileVO(User user) {
        UserProfileVO userProfileVO = new UserProfileVO();
        userProfileVO.setId(user.getId());
        userProfileVO.setUsername(user.getUsername());
        userProfileVO.setNickname(user.getNickname());
        userProfileVO.setAvatarUrl(user.getAvatarUrl());
        userProfileVO.setMajor(user.getMajor());
        userProfileVO.setCampusVerifyStatus(user.getCampusVerifyStatus());
        return userProfileVO;
    }

    private String buildToken(User user) {
        boolean isAdmin = user.getRole() == UserRoleEnum.ADMIN;
        String secretKey = isAdmin ? jwtProperties.getAdminSecretKey() : jwtProperties.getUserSecretKey();
        long ttl = isAdmin ? jwtProperties.getAdminTtl() : jwtProperties.getUserTtl();

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("role", user.getRole().getCode());
        claims.put("tokenType", isAdmin ? "admin" : "user");

        return JwtUtil.createJWT(secretKey, ttl, claims);
    }

    private void clearCampusVerifyAuditFields(User user) {
        user.setCampusVerifyRemark(null);
        user.setCampusVerifyTime(null);
        user.setCampusVerifyAdminId(null);
    }

    private void clearUnbanAuditFields(User user) {
        user.setUnbanReason(null);
        user.setUnbanTime(null);
        user.setUnbanAdminId(null);
    }

    private void validateUserRole(User user, String message) {
        if (user.getRole() != UserRoleEnum.USER) {
            throw new BaseException(message);
        }
    }

    private void requireAdminId(Long adminId) {
        if (adminId == null) {
            throw new BaseException("管理员未登录");
        }
    }

    private String requireReason(String reason, String message) {
        String trimmedReason = trimToNull(reason);
        if (!StringUtils.hasText(trimmedReason)) {
            throw new BaseException(message);
        }
        return trimmedReason;
    }

    private long normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
    }

    private long normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
