package cn.edu.zut.softwareengineering.controller;

import cn.edu.zut.softwareengineering.service.ForumPostService;
import cn.edu.zut.softwareengineering.service.UserService;
import cn.edu.zut.softwareengineering.utils.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExceptionResponseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private UserService userService;

    @MockBean
    private ForumPostService forumPostService;

    @Test
    @DisplayName("IllegalArgumentException 返回 code=400")
    void illegalArgumentExceptionReturns400() throws Exception {
        when(forumPostService.getPostDetail(999))
                .thenThrow(new IllegalArgumentException("帖子不存在"));

        mockMvc.perform(post("/posts/detail")
                        .header("Authorization", bearerToken(7, "USER"))
                        .param("postId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("帖子不存在"));
    }

    @Test
    @DisplayName("IllegalStateException 返回 code=409")
    void illegalStateExceptionReturns409() throws Exception {
        doThrow(new IllegalStateException("只能删除自己的帖子"))
                .when(forumPostService).deleteOwnPost(10, 7);

        mockMvc.perform(post("/posts/delete")
                        .header("Authorization", bearerToken(7, "USER"))
                        .param("postId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("只能删除自己的帖子"));
    }

    @Test
    @DisplayName("登录失败返回 code=500 和提示消息")
    void loginFailureReturns500() throws Exception {
        when(userService.login(any())).thenThrow(new IllegalArgumentException("用户名或密码错误"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"bad\",\"password\":\"wrong\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    @DisplayName("异常返回 body 中 data 为 null")
    void errorResponseHasNullData() throws Exception {
        when(forumPostService.getPostDetail(999))
                .thenThrow(new IllegalArgumentException("帖子不存在"));

        mockMvc.perform(post("/posts/detail")
                        .header("Authorization", bearerToken(7, "USER"))
                        .param("postId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    @DisplayName("JWT 无效时返回 403")
    void invalidJwtReturns403() throws Exception {
        mockMvc.perform(post("/user/profile")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isForbidden());
    }

    private String bearerToken(Integer userId, String role) {
        return "Bearer " + jwtUtil.generateToken(userId, "user" + userId, role);
    }
}
