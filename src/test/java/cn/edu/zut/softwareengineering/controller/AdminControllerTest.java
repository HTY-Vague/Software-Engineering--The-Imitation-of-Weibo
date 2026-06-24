package cn.edu.zut.softwareengineering.controller;

import cn.edu.zut.softwareengineering.entity.ForumBoard;
import cn.edu.zut.softwareengineering.entity.ForumPost;
import cn.edu.zut.softwareengineering.entity.StatHotPost;
import cn.edu.zut.softwareengineering.entity.User;
import cn.edu.zut.softwareengineering.service.ForumBoardService;
import cn.edu.zut.softwareengineering.service.ForumCommentService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private UserService userService;

    @MockBean
    private ForumBoardService forumBoardService;

    @MockBean
    private ForumPostService forumPostService;

    @MockBean
    private ForumCommentService forumCommentService;

    @MockBean
    private StatisticsService statisticsService;

    // ---- users ----

    @Test
    @DisplayName("管理员获取用户列表返回统一响应")
    void adminGetUsersReturnsUnifiedResponse() throws Exception {
        User user = new User();
        user.setUserId(1);
        user.setUsername("test");
        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/admin/users")
                        .header("Authorization", adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].username").value("test"));
    }

    @Test
    @DisplayName("管理员更新用户状态")
    void adminUpdateUserStatus() throws Exception {
        mockMvc.perform(put("/admin/users/1/status")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).updateUserStatus(1, 0);
    }

    // ---- boards ----

    @Test
    @DisplayName("管理员新增板块")
    void adminAddBoard() throws Exception {
        ForumBoard board = new ForumBoard();
        board.setBoardName("新板块");
        board.setDescription("描述");

        mockMvc.perform(post("/admin/forums")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(board)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(forumBoardService).addBoard(any(ForumBoard.class));
    }

    @Test
    @DisplayName("管理员更新板块")
    void adminUpdateBoard() throws Exception {
        ForumBoard board = new ForumBoard();
        board.setBoardName("更新板块");

        mockMvc.perform(put("/admin/forums/1")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(board)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(forumBoardService).updateBoard(any(ForumBoard.class));
    }

    @Test
    @DisplayName("管理员更新板块状态")
    void adminUpdateBoardStatus() throws Exception {
        mockMvc.perform(put("/admin/forums/1/status")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(forumBoardService).updateBoardStatus(1, 0);
    }

    @Test
    @DisplayName("管理员排序板块")
    void adminSortForums() throws Exception {
        List<Long> boardIds = List.of(3L, 1L, 2L);

        mockMvc.perform(put("/admin/forums/sort")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(boardIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(forumBoardService).sortBoards(boardIds);
    }

    @Test
    @DisplayName("管理员删除板块")
    void adminDeleteBoard() throws Exception {
        mockMvc.perform(delete("/admin/forums/1")
                        .header("Authorization", adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(forumBoardService).deleteBoard(1);
    }

    // ---- posts ----

    @Test
    @DisplayName("管理员置顶帖子")
    void adminToggleTop() throws Exception {
        mockMvc.perform(put("/admin/posts/10/top")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(forumPostService).toggleTop(10, 1);
    }

    @Test
    @DisplayName("管理员删除帖子")
    void adminDeletePost() throws Exception {
        mockMvc.perform(delete("/admin/posts/10")
                        .header("Authorization", adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(forumPostService).deletePost(10);
    }

    // ---- comments ----

    @Test
    @DisplayName("管理员删除评论")
    void adminDeleteComment() throws Exception {
        mockMvc.perform(delete("/admin/comments/20")
                        .header("Authorization", adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(forumCommentService).deleteComment(20);
    }

    // ---- statistics ----

    @Test
    @DisplayName("管理员获取统计概览")
    void adminGetStatisticsOverview() throws Exception {
        when(statisticsService.getOverview()).thenReturn(Map.of("userCount", 42, "postCount", 100));

        mockMvc.perform(get("/admin/statistics/overview")
                        .header("Authorization", adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userCount").value(42))
                .andExpect(jsonPath("$.data.postCount").value(100));
    }

    @Test
    @DisplayName("管理员获取热门帖子，支持自定义limit")
    void adminGetHotPosts() throws Exception {
        StatHotPost post = new StatHotPost();
        post.setRankNum(1);
        post.setPostId(10L);
        post.setTitle("热门");
        post.setHotScore(99.5);
        when(statisticsService.getHotPosts(5)).thenReturn(List.of(post));

        mockMvc.perform(get("/admin/statistics/hot-posts")
                        .header("Authorization", adminToken())
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].postId").value(10))
                .andExpect(jsonPath("$.data[0].hotScore").value(99.5));

        verify(statisticsService).getHotPosts(5);
    }

    @Test
    @DisplayName("普通用户访问管理员接口被拒绝")
    void userCannotAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .header("Authorization", userToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("未登录访问管理员接口被拦截")
    void unauthenticatedAccessToAdminIsBlocked() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    private String adminToken() {
        return "Bearer " + jwtUtil.generateToken(1, "admin", "ADMIN");
    }

    private String userToken() {
        return "Bearer " + jwtUtil.generateToken(7, "user", "USER");
    }
}
