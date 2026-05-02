package com.cshm.campussecondhandmark.module.user.controller;

import com.cshm.campussecondhandmark.common.context.BaseContext;
import com.cshm.campussecondhandmark.module.user.enums.CampusVerifyStatusEnum;
import com.cshm.campussecondhandmark.module.user.enums.UserRoleEnum;
import com.cshm.campussecondhandmark.module.user.pojo.vo.CurrentUserVO;
import com.cshm.campussecondhandmark.module.user.pojo.vo.UserLoginVO;
import com.cshm.campussecondhandmark.module.user.pojo.vo.UserProfileVO;
import com.cshm.campussecondhandmark.module.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        UserController userController = new UserController();
        ReflectionTestUtils.setField(userController, "userService", userService);
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setValidator(new Validator() {
                    @Override
                    public boolean supports(Class<?> clazz) {
                        return true;
                    }

                    @Override
                    public void validate(Object target, Errors errors) {
                    }
                })
                .build();
    }

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    @Test
    void loginShouldSupportApiAuthPath() throws Exception {
        UserLoginVO userLoginVO = new UserLoginVO();
        userLoginVO.setId(1L);
        userLoginVO.setNickname("张三");
        userLoginVO.setRole(UserRoleEnum.USER);
        userLoginVO.setToken("user-token");

        when(userService.login(any())).thenReturn(userLoginVO);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"zhangsan@example.com\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(userService).login(any());
    }

    @Test
    void getCurrentUserShouldSupportApiUserMePath() throws Exception {
        CurrentUserVO currentUserVO = new CurrentUserVO();
        currentUserVO.setId(1L);
        currentUserVO.setUsername("zhangsan");
        currentUserVO.setNickname("张三");
        currentUserVO.setCampusVerifyStatus(CampusVerifyStatusEnum.PENDING);

        BaseContext.setCurrentId(1L);
        when(userService.getCurrentUser(1L)).thenReturn(currentUserVO);

        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.username").value("zhangsan"));

        verify(userService).getCurrentUser(1L);
    }

    @Test
    void getUserProfileShouldSupportApiUsersProfilePath() throws Exception {
        UserProfileVO userProfileVO = new UserProfileVO();
        userProfileVO.setId(2L);
        userProfileVO.setUsername("lisi");
        userProfileVO.setNickname("李四");
        userProfileVO.setCampusVerifyStatus(CampusVerifyStatusEnum.APPROVED);

        when(userService.getUserProfile(2L)).thenReturn(userProfileVO);

        mockMvc.perform(get("/api/users/2/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.nickname").value("李四"));

        verify(userService).getUserProfile(2L);
    }

    @Test
    void updateProfileShouldSupportApiUserProfilePath() throws Exception {
        BaseContext.setCurrentId(3L);

        mockMvc.perform(put("/api/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateProfileRequest("王五", "13800000000", "20240003", "人工智能", "http://example.com/avatar.jpg"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(userService).updateProfile(eq(3L), any());
    }

    @Test
    void legacyUserPathsShouldNotBeSupported() throws Exception {
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"zhangsan@example.com\",\"password\":\"123456\"}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/user/me"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/user/2"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/user/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateProfileRequest("王五", "13800000000", "20240003", "人工智能", "http://example.com/avatar.jpg"))))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/user/logout"))
                .andExpect(status().isNotFound());
    }

    private static class UpdateProfileRequest {
        public String nickname;
        public String phone;
        public String studentNo;
        public String major;
        public String avatarUrl;

        public UpdateProfileRequest(String nickname, String phone, String studentNo, String major, String avatarUrl) {
            this.nickname = nickname;
            this.phone = phone;
            this.studentNo = studentNo;
            this.major = major;
            this.avatarUrl = avatarUrl;
        }
    }
}
