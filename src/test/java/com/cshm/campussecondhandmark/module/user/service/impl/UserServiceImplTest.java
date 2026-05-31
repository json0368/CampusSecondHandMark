package com.cshm.campussecondhandmark.module.user.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cshm.campussecondhandmark.common.exception.BaseException;
import com.cshm.campussecondhandmark.common.properties.JwtProperties;
import com.cshm.campussecondhandmark.common.result.PageResult;
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
import com.cshm.campussecondhandmark.module.user.pojo.entity.User;
import com.cshm.campussecondhandmark.module.user.pojo.vo.AdminUserDetailVO;
import com.cshm.campussecondhandmark.module.user.pojo.vo.AdminUserListVO;
import com.cshm.campussecondhandmark.module.user.pojo.vo.CampusVerifyAuditVO;
import com.cshm.campussecondhandmark.module.user.pojo.vo.CurrentUserVO;
import com.cshm.campussecondhandmark.module.user.pojo.vo.UserLoginVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl();
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
        ReflectionTestUtils.setField(userService, "passwordEncoder", passwordEncoder);

        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setTokenName("token");
        jwtProperties.setUserSecretKey("user-secret");
        jwtProperties.setUserTtl(7200000L);
        jwtProperties.setAdminSecretKey("admin-secret");
        jwtProperties.setAdminTtl(7200000L);
        ReflectionTestUtils.setField(userService, "jwtProperties", jwtProperties);
    }

    @Test
    void registerShouldSaveUserAndReturnLoginVO() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("zhangsan");
        dto.setNickname("张三");
        dto.setEmail("zhangsan@example.com");
        dto.setStudentNo("20240001");
        dto.setMajor("软件工程");
        dto.setPassword("123456");

        when(userMapper.selectCount(any())).thenReturn(0L, 0L, 0L);
        when(passwordEncoder.encode("123456")).thenReturn("加密后的密码");
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return 1;
        }).when(userMapper).insert(any(User.class));

        UserLoginVO vo = userService.register(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        User savedUser = captor.getValue();

        assertEquals("zhangsan", savedUser.getUsername());
        assertEquals("张三", savedUser.getNickname());
        assertEquals("zhangsan@example.com", savedUser.getEmail());
        assertNull(savedUser.getPhone());
        assertEquals("20240001", savedUser.getStudentNo());
        assertEquals("软件工程", savedUser.getMajor());
        assertEquals(UserRoleEnum.USER, savedUser.getRole());
        assertEquals(UserStatusEnum.NORMAL, savedUser.getStatus());
        assertEquals(CampusVerifyStatusEnum.PENDING, savedUser.getCampusVerifyStatus());
        assertEquals("加密后的密码", savedUser.getPasswordHash());
        assertNotEquals("123456", savedUser.getPasswordHash());
        assertNotNull(savedUser.getLastLoginTime());

        assertEquals(1L, vo.getId());
        assertEquals(UserRoleEnum.USER, vo.getRole());
        assertEquals("张三", vo.getNickname());
        assertNotNull(vo.getToken());
    }

    @Test
    void registerShouldThrowWhenEmailAlreadyRegistered() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("zhangsan");
        dto.setNickname("张三");
        dto.setEmail("zhangsan@example.com");
        dto.setStudentNo("20240001");
        dto.setMajor("软件工程");
        dto.setPassword("123456");

        when(userMapper.selectCount(any())).thenReturn(0L, 1L);

        BaseException exception = assertThrows(BaseException.class, () -> userService.register(dto));

        assertEquals("邮箱已被注册", exception.getMessage());
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void loginShouldThrowWhenAdminUsesUserEndpoint() {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setEmail("admin@example.com");
        dto.setPassword("123456");

        User admin = new User();
        admin.setId(10L);
        admin.setEmail("admin@example.com");
        admin.setPasswordHash("加密密码");
        admin.setRole(UserRoleEnum.ADMIN);
        admin.setStatus(UserStatusEnum.NORMAL);

        when(userMapper.selectOne(any())).thenReturn(admin);
        when(passwordEncoder.matches("123456", "加密密码")).thenReturn(true);

        BaseException exception = assertThrows(BaseException.class, () -> userService.login(dto));

        assertEquals("管理员账号请使用后台登录", exception.getMessage());
    }

    @Test
    void adminLoginShouldReturnAdminToken() {
        AdminLoginDTO dto = new AdminLoginDTO();
        dto.setEmail("admin@example.com");
        dto.setPassword("123456");

        User admin = new User();
        admin.setId(10L);
        admin.setNickname("管理员");
        admin.setEmail("admin@example.com");
        admin.setPasswordHash("加密密码");
        admin.setRole(UserRoleEnum.ADMIN);
        admin.setStatus(UserStatusEnum.NORMAL);

        when(userMapper.selectOne(any())).thenReturn(admin);
        when(passwordEncoder.matches("123456", "加密密码")).thenReturn(true);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        AdminLoginVO vo = userService.adminLogin(dto);

        assertEquals(10L, vo.getId());
        assertEquals("管理员", vo.getNickname());
        assertNotNull(vo.getToken());
    }

    @Test
    void getCurrentUserShouldThrowWhenUserNotFound() {
        when(userMapper.selectById(1L)).thenReturn(null);

        BaseException exception = assertThrows(BaseException.class, () -> userService.getCurrentUser(1L));

        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    void getCurrentUserShouldReturnCurrentUserVO() {
        User user = new User();
        user.setId(1L);
        user.setUsername("zhangsan");
        user.setNickname("张三");
        user.setEmail("zhangsan@example.com");
        user.setPhone("13800000000");
        user.setStudentNo("20240001");
        user.setMajor("软件工程");
        user.setAvatarUrl("http://example.com/avatar.jpg");
        user.setRole(UserRoleEnum.USER);
        user.setStatus(UserStatusEnum.NORMAL);
        user.setCampusVerifyStatus(CampusVerifyStatusEnum.APPROVED);

        when(userMapper.selectById(1L)).thenReturn(user);

        CurrentUserVO vo = userService.getCurrentUser(1L);

        assertEquals(1L, vo.getId());
        assertEquals("zhangsan", vo.getUsername());
        assertEquals("张三", vo.getNickname());
        assertEquals("zhangsan@example.com", vo.getEmail());
        assertEquals("13800000000", vo.getPhone());
        assertEquals("20240001", vo.getStudentNo());
        assertEquals("软件工程", vo.getMajor());
        assertEquals(CampusVerifyStatusEnum.APPROVED, vo.getCampusVerifyStatus());
    }

    @Test
    void updateProfileShouldResetCampusVerifyStatusWhenCampusInfoChanges() {
        User user = new User();
        user.setId(1L);
        user.setNickname("张三");
        user.setPhone("13800000000");
        user.setStudentNo("20240001");
        user.setMajor("软件工程");
        user.setAvatarUrl("http://example.com/old-avatar.jpg");
        user.setRole(UserRoleEnum.USER);
        user.setStatus(UserStatusEnum.NORMAL);
        user.setCampusVerifyStatus(CampusVerifyStatusEnum.APPROVED);

        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setNickname("张三-新");
        dto.setPhone("13900000000");
        dto.setStudentNo("20240002");
        dto.setMajor("计算机科学与技术");
        dto.setAvatarUrl("http://example.com/new-avatar.jpg");

        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.selectCount(any())).thenReturn(0L, 0L);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        userService.updateProfile(1L, dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(captor.capture());
        User updatedUser = captor.getValue();

        assertEquals("张三-新", updatedUser.getNickname());
        assertEquals("13900000000", updatedUser.getPhone());
        assertEquals("20240002", updatedUser.getStudentNo());
        assertEquals("计算机科学与技术", updatedUser.getMajor());
        assertEquals("http://example.com/new-avatar.jpg", updatedUser.getAvatarUrl());
        assertEquals(CampusVerifyStatusEnum.PENDING, updatedUser.getCampusVerifyStatus());
        assertNotNull(updatedUser.getUpdateTime());
    }

    @Test
    void auditCampusVerifyShouldApprovePendingUserAndRecordAuditMetadata() {
        User user = new User();
        user.setId(1L);
        user.setRole(UserRoleEnum.USER);
        user.setCampusVerifyStatus(CampusVerifyStatusEnum.PENDING);

        CampusVerifyAuditDTO dto = new CampusVerifyAuditDTO();
        dto.setCampusVerifyStatus(CampusVerifyStatusEnum.APPROVED);
        dto.setRemark("材料已核验");

        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        userService.auditCampusVerify(1L, 99L, dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(captor.capture());
        User updatedUser = captor.getValue();

        assertEquals(CampusVerifyStatusEnum.APPROVED, updatedUser.getCampusVerifyStatus());
        assertEquals("材料已核验", updatedUser.getCampusVerifyRemark());
        assertEquals(99L, updatedUser.getCampusVerifyAdminId());
        assertNotNull(updatedUser.getCampusVerifyTime());
    }

    @Test
    void auditCampusVerifyShouldRejectPendingUserAndRequireReason() {
        User user = new User();
        user.setId(1L);
        user.setRole(UserRoleEnum.USER);
        user.setCampusVerifyStatus(CampusVerifyStatusEnum.PENDING);

        CampusVerifyAuditDTO dto = new CampusVerifyAuditDTO();
        dto.setCampusVerifyStatus(CampusVerifyStatusEnum.REJECTED);

        when(userMapper.selectById(1L)).thenReturn(user);

        BaseException exception = assertThrows(BaseException.class, () -> userService.auditCampusVerify(1L, 99L, dto));

        assertEquals("驳回原因不能为空", exception.getMessage());
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    void updateProfileShouldClearCampusAuditMetadataWhenCampusInfoChanges() {
        User user = new User();
        user.setId(1L);
        user.setNickname("张三");
        user.setStudentNo("20240001");
        user.setMajor("软件工程");
        user.setRole(UserRoleEnum.USER);
        user.setCampusVerifyStatus(CampusVerifyStatusEnum.REJECTED);
        user.setCampusVerifyRemark("学号不匹配");
        user.setCampusVerifyAdminId(8L);
        user.setCampusVerifyTime(LocalDateTime.now().minusDays(1));

        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setStudentNo("20240002");

        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        userService.updateProfile(1L, dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(captor.capture());
        User updatedUser = captor.getValue();

        assertEquals(CampusVerifyStatusEnum.PENDING, updatedUser.getCampusVerifyStatus());
        assertNull(updatedUser.getCampusVerifyRemark());
        assertNull(updatedUser.getCampusVerifyAdminId());
        assertNull(updatedUser.getCampusVerifyTime());
    }

    @Test
    void pageCampusVerifyUsersShouldReturnAuditView() {
        User user = new User();
        user.setId(1L);
        user.setUsername("zhangsan");
        user.setNickname("张三");
        user.setStudentNo("20240001");
        user.setMajor("软件工程");
        user.setCampusVerifyStatus(CampusVerifyStatusEnum.PENDING);

        Page<User> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(user));

        when(userMapper.selectPage(any(Page.class), any())).thenReturn(page);

        PageResult<CampusVerifyAuditVO> result = userService.pageCampusVerifyUsers(new CampusVerifyQueryDTO());

        assertEquals(1, result.getTotal());
        assertEquals("zhangsan", result.getRecords().get(0).getUsername());
        assertEquals(CampusVerifyStatusEnum.PENDING, result.getRecords().get(0).getCampusVerifyStatus());
    }

    @Test
    void pageAdminUsersShouldReturnPagedUserList() {
        User user = new User();
        user.setId(1L);
        user.setUsername("zhangsan");
        user.setNickname("张三");
        user.setEmail("zhangsan@example.com");
        user.setPhone("13800000000");
        user.setStudentNo("20240001");
        user.setMajor("软件工程");
        user.setRole(UserRoleEnum.USER);
        user.setStatus(UserStatusEnum.BANNED);
        user.setCampusVerifyStatus(CampusVerifyStatusEnum.APPROVED);
        user.setBanReason("发布违规商品");
        user.setBanAdminId(7L);
        user.setBanTime(LocalDateTime.now().minusHours(2));

        Page<User> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(user));

        when(userMapper.selectPage(any(Page.class), any())).thenReturn(page);

        PageResult<AdminUserListVO> result = userService.pageAdminUsers(new AdminUserQueryDTO());

        assertEquals(1, result.getTotal());
        assertEquals("zhangsan", result.getRecords().get(0).getUsername());
        assertEquals(UserStatusEnum.BANNED, result.getRecords().get(0).getStatus());
        assertEquals("发布违规商品", result.getRecords().get(0).getBanReason());
    }

    @Test
    void getAdminUserDetailShouldReturnUserDetail() {
        User user = new User();
        user.setId(1L);
        user.setUsername("zhangsan");
        user.setNickname("张三");
        user.setEmail("zhangsan@example.com");
        user.setPhone("13800000000");
        user.setStudentNo("20240001");
        user.setMajor("软件工程");
        user.setAvatarUrl("http://example.com/avatar.jpg");
        user.setRole(UserRoleEnum.USER);
        user.setStatus(UserStatusEnum.NORMAL);
        user.setCampusVerifyStatus(CampusVerifyStatusEnum.APPROVED);
        user.setBanReason("历史封禁");
        user.setBanAdminId(7L);
        user.setBanTime(LocalDateTime.now().minusDays(3));
        user.setUnbanReason("已完成整改");
        user.setUnbanAdminId(9L);
        user.setUnbanTime(LocalDateTime.now().minusDays(1));

        when(userMapper.selectById(1L)).thenReturn(user);

        AdminUserDetailVO detailVO = userService.getAdminUserDetail(1L);

        assertEquals(1L, detailVO.getId());
        assertEquals("zhangsan", detailVO.getUsername());
        assertEquals("历史封禁", detailVO.getBanReason());
        assertEquals("已完成整改", detailVO.getUnbanReason());
        assertEquals(UserRoleEnum.USER, detailVO.getRole());
    }

    @Test
    void banUserShouldUpdateStatusAndBanMetadata() {
        User user = new User();
        user.setId(1L);
        user.setRole(UserRoleEnum.USER);
        user.setStatus(UserStatusEnum.NORMAL);
        user.setUnbanReason("旧解封原因");
        user.setUnbanAdminId(5L);
        user.setUnbanTime(LocalDateTime.now().minusDays(1));

        AdminUserBanDTO dto = new AdminUserBanDTO();
        dto.setReason("发布违规商品");

        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        userService.banUser(1L, 7L, dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(captor.capture());
        User updatedUser = captor.getValue();

        assertEquals(UserStatusEnum.BANNED, updatedUser.getStatus());
        assertEquals("发布违规商品", updatedUser.getBanReason());
        assertEquals(7L, updatedUser.getBanAdminId());
        assertNotNull(updatedUser.getBanTime());
        assertNull(updatedUser.getUnbanReason());
        assertNull(updatedUser.getUnbanAdminId());
        assertNull(updatedUser.getUnbanTime());
    }

    @Test
    void unbanUserShouldUpdateStatusAndUnbanMetadata() {
        User user = new User();
        user.setId(1L);
        user.setRole(UserRoleEnum.USER);
        user.setStatus(UserStatusEnum.BANNED);
        user.setBanReason("发布违规商品");
        user.setBanAdminId(7L);
        LocalDateTime banTime = LocalDateTime.now().minusDays(2);
        user.setBanTime(banTime);

        AdminUserUnbanDTO dto = new AdminUserUnbanDTO();
        dto.setReason("已完成整改");

        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        userService.unbanUser(1L, 9L, dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(captor.capture());
        User updatedUser = captor.getValue();

        assertEquals(UserStatusEnum.NORMAL, updatedUser.getStatus());
        assertEquals("发布违规商品", updatedUser.getBanReason());
        assertEquals(banTime, updatedUser.getBanTime());
        assertEquals("已完成整改", updatedUser.getUnbanReason());
        assertEquals(9L, updatedUser.getUnbanAdminId());
        assertNotNull(updatedUser.getUnbanTime());
    }

    @Test
    void banUserShouldRejectAdminTarget() {
        User user = new User();
        user.setId(1L);
        user.setRole(UserRoleEnum.ADMIN);
        user.setStatus(UserStatusEnum.NORMAL);

        AdminUserBanDTO dto = new AdminUserBanDTO();
        dto.setReason("违规");

        when(userMapper.selectById(1L)).thenReturn(user);

        BaseException exception = assertThrows(BaseException.class, () -> userService.banUser(1L, 7L, dto));

        assertEquals("封禁目标必须是普通用户", exception.getMessage());
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    void unbanUserShouldRejectNormalTarget() {
        User user = new User();
        user.setId(1L);
        user.setRole(UserRoleEnum.USER);
        user.setStatus(UserStatusEnum.NORMAL);

        AdminUserUnbanDTO dto = new AdminUserUnbanDTO();
        dto.setReason("恢复");

        when(userMapper.selectById(1L)).thenReturn(user);

        BaseException exception = assertThrows(BaseException.class, () -> userService.unbanUser(1L, 7L, dto));

        assertEquals("当前用户不是封禁状态", exception.getMessage());
        verify(userMapper, never()).updateById(any(User.class));
    }
}
