package cn.edu.zut.softwareengineering.controller;

import cn.edu.zut.softwareengineering.entity.User;
import cn.edu.zut.softwareengineering.service.ForumBoardService;
import cn.edu.zut.softwareengineering.service.ForumCommentService;
import cn.edu.zut.softwareengineering.service.ForumLikeService;
import cn.edu.zut.softwareengineering.service.ForumPostService;
import cn.edu.zut.softwareengineering.service.StatisticsService;
import cn.edu.zut.softwareengineering.service.UserService;
import cn.edu.zut.softwareengineering.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private UserService userService;

    @MockBean
    private ForumPostService forumPostService;

    @MockBean
    private ForumBoardService forumBoardService;

    @MockBean
    private ForumCommentService forumCommentService;

    @MockBean
    private ForumLikeService forumLikeService;

    @MockBean
    private StatisticsService statisticsService;

    @Test
    @DisplayName("登录成功时返回统一响应和JWT")
    void loginReturnsToken() throws Exception {
        User loginUser = new User();
        loginUser.setUsername("test");
        loginUser.setPassword("123456");

        User dbUser = new User();
        dbUser.setUserId(1);
        dbUser.setUsername("test");
        dbUser.setNickname("测试用户");
        dbUser.setRole("USER");

        when(userService.login(any(User.class))).thenReturn(dbUser);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value(not(nullValue())))
                .andExpect(jsonPath("$.data.user.username").value("test"));
    }

    @Test
    @DisplayName("未登录访问受保护接口时被拦截")
    void protectedEndpointRequiresLogin() throws Exception {
        mockMvc.perform(post("/posts/list"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("携带有效USER令牌可访问普通业务接口")
    void userTokenCanAccessProtectedEndpoint() throws Exception {
        String token = jwtUtil.generateToken(1, "test", "USER");
        when(forumPostService.getPostList(null, null, null, null)).thenReturn(List.of());

        mockMvc.perform(post("/posts/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("USER令牌访问管理员接口时被拒绝")
    void userTokenCannotAccessAdminEndpoint() throws Exception {
        String token = jwtUtil.generateToken(1, "test", "USER");

        mockMvc.perform(get("/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN令牌可访问管理员接口")
    void adminTokenCanAccessAdminEndpoint() throws Exception {
        String token = jwtUtil.generateToken(1, "admin", "ADMIN");
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("ADMIN令牌可访问后台统计概览")
    void adminTokenCanAccessStatisticsOverview() throws Exception {
        String token = jwtUtil.generateToken(1, "admin", "ADMIN");
        when(statisticsService.getOverview()).thenReturn(Map.of("postCount", 20));

        mockMvc.perform(get("/admin/statistics/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.postCount").value(20));
    }
}
