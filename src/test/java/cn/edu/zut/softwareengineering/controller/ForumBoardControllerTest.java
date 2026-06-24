package cn.edu.zut.softwareengineering.controller;

import cn.edu.zut.softwareengineering.entity.ForumBoard;
import cn.edu.zut.softwareengineering.entity.ForumPost;
import cn.edu.zut.softwareengineering.service.ForumBoardService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ForumBoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private ForumBoardService forumBoardService;

    @Test
    @DisplayName("板块列表接口返回统一响应")
    void getBoardsReturnsUnifiedResponse() throws Exception {
        ForumBoard board = new ForumBoard();
        board.setBoardId(1);
        board.setBoardName("测试板块");
        when(forumBoardService.getBoards()).thenReturn(List.of(board));

        mockMvc.perform(post("/forum/boards")
                        .header("Authorization", bearerToken(7, "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].boardId").value(1))
                .andExpect(jsonPath("$.data[0].boardName").value("测试板块"));
    }

    @Test
    @DisplayName("板块列表为空时返回空数组")
    void getBoardsReturnsEmptyArray() throws Exception {
        when(forumBoardService.getBoards()).thenReturn(List.of());

        mockMvc.perform(post("/forum/boards")
                        .header("Authorization", bearerToken(7, "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("板块帖子接口返回指定板块下的帖子")
    void getBoardPostsReturnsPosts() throws Exception {
        ForumBoard request = new ForumBoard();
        request.setBoardId(1);

        ForumPost post = new ForumPost();
        post.setPostId(10);
        post.setBoardId(1);
        post.setTitle("帖子标题");

        when(forumBoardService.getBoardPosts(any(ForumBoard.class))).thenReturn(List.of(post));

        mockMvc.perform(post("/forum/board/posts")
                        .header("Authorization", bearerToken(7, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].postId").value(10))
                .andExpect(jsonPath("$.data[0].title").value("帖子标题"));
    }

    @Test
    @DisplayName("未登录时访问受保护接口被拦截")
    void unauthenticatedAccessIsBlocked() throws Exception {
        mockMvc.perform(post("/user/profile"))
                .andExpect(status().isForbidden());
    }

    private String bearerToken(Integer userId, String role) {
        return "Bearer " + jwtUtil.generateToken(userId, "user" + userId, role);
    }
}
