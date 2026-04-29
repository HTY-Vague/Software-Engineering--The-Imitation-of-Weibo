package cn.edu.zut.softwareengineering.controller;

import cn.edu.zut.softwareengineering.entity.ForumComment;
import cn.edu.zut.softwareengineering.entity.ForumPost;
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
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ForumBusinessControllerTest {

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
    @DisplayName("发帖时使用JWT中的当前用户作为作者")
    void createPostUsesCurrentUserAsAuthor() throws Exception {
        ForumPost request = new ForumPost();
        request.setBoardId(2);
        request.setAuthorId(999);
        request.setTitle("title");
        request.setContent("content");

        ForumPost created = new ForumPost();
        created.setPostId(10);
        created.setBoardId(2);
        created.setAuthorId(7);
        created.setTitle("title");
        created.setContent("content");

        when(forumPostService.createPost(org.mockito.ArgumentMatchers.any(ForumPost.class))).thenReturn(created);

        mockMvc.perform(post("/posts/create")
                        .header("Authorization", bearerToken(7, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.postId").value(10))
                .andExpect(jsonPath("$.data.authorId").value(7));

        ArgumentCaptor<ForumPost> postCaptor = ArgumentCaptor.forClass(ForumPost.class);
        verify(forumPostService).createPost(postCaptor.capture());
        assertEquals(7, postCaptor.getValue().getAuthorId());
    }

    @Test
    @DisplayName("帖子列表接口透传查询参数并返回统一响应")
    void postListForwardsQueryParams() throws Exception {
        ForumPost post = new ForumPost();
        post.setPostId(11);
        post.setBoardId(2);
        post.setTitle("hello");
        when(forumPostService.getPostList(2, "hello", 1, 20)).thenReturn(List.of(post));

        mockMvc.perform(post("/posts/list")
                        .header("Authorization", bearerToken(7, "USER"))
                        .param("boardId", "2")
                        .param("keyword", "hello")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].postId").value(11));

        verify(forumPostService).getPostList(2, "hello", 1, 20);
    }

    @Test
    @DisplayName("编辑帖子时使用JWT中的当前用户做作者权限校验")
    void updatePostUsesCurrentUser() throws Exception {
        ForumPost request = new ForumPost();
        request.setPostId(10);
        request.setTitle("new title");
        request.setContent("new content");

        mockMvc.perform(post("/posts/update")
                        .header("Authorization", bearerToken(7, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(forumPostService).updateOwnPost(org.mockito.ArgumentMatchers.any(ForumPost.class), eq(7));
    }

    @Test
    @DisplayName("删除帖子时使用JWT中的当前用户做作者权限校验")
    void deletePostUsesCurrentUser() throws Exception {
        mockMvc.perform(post("/posts/delete")
                        .header("Authorization", bearerToken(7, "USER"))
                        .param("postId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(forumPostService).deleteOwnPost(10, 7);
    }

    @Test
    @DisplayName("发表评论时使用JWT中的当前用户作为评论作者")
    void createCommentUsesCurrentUser() throws Exception {
        ForumComment request = new ForumComment();
        request.setPostId(10);
        request.setUserId(999);
        request.setContent("comment");

        ForumComment created = new ForumComment();
        created.setCommentId(20);
        created.setPostId(10);
        created.setUserId(7);
        created.setContent("comment");

        when(forumCommentService.createComment(org.mockito.ArgumentMatchers.any(ForumComment.class))).thenReturn(created);

        mockMvc.perform(post("/comments/create")
                        .header("Authorization", bearerToken(7, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.commentId").value(20))
                .andExpect(jsonPath("$.data.userId").value(7));

        ArgumentCaptor<ForumComment> commentCaptor = ArgumentCaptor.forClass(ForumComment.class);
        verify(forumCommentService).createComment(commentCaptor.capture());
        assertEquals(7, commentCaptor.getValue().getUserId());
    }

    @Test
    @DisplayName("评论列表接口返回统一响应")
    void commentListReturnsUnifiedResponse() throws Exception {
        ForumComment comment = new ForumComment();
        comment.setCommentId(20);
        comment.setPostId(10);
        comment.setContent("comment");
        when(forumCommentService.getComments(10)).thenReturn(List.of(comment));

        mockMvc.perform(post("/comments/list")
                        .header("Authorization", bearerToken(7, "USER"))
                        .param("postId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].commentId").value(20));

        verify(forumCommentService).getComments(10);
    }

    @Test
    @DisplayName("删除评论时使用JWT中的当前用户做作者权限校验")
    void deleteCommentUsesCurrentUser() throws Exception {
        mockMvc.perform(post("/comments/delete")
                        .header("Authorization", bearerToken(7, "USER"))
                        .param("commentId", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(forumCommentService).deleteOwnComment(20, 7);
    }

    @Test
    @DisplayName("点赞和取消点赞使用JWT中的当前用户")
    void likeActionsUseCurrentUser() throws Exception {
        mockMvc.perform(post("/likes/like")
                        .header("Authorization", bearerToken(7, "USER"))
                        .param("targetType", "1")
                        .param("targetId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/likes/unlike")
                        .header("Authorization", bearerToken(7, "USER"))
                        .param("targetType", "1")
                        .param("targetId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(forumLikeService).like(1, 10, 7);
        verify(forumLikeService).unlike(1, 10, 7);
    }

    @Test
    @DisplayName("点赞状态和数量接口返回统一响应")
    void likeReadApisReturnUnifiedResponse() throws Exception {
        when(forumLikeService.hasLiked(1, 10, 7)).thenReturn(true);
        when(forumLikeService.countLikes(1, 10)).thenReturn(3);

        mockMvc.perform(post("/likes/status")
                        .header("Authorization", bearerToken(7, "USER"))
                        .param("targetType", "1")
                        .param("targetId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        mockMvc.perform(post("/likes/count")
                        .header("Authorization", bearerToken(7, "USER"))
                        .param("targetType", "1")
                        .param("targetId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(3));

        verify(forumLikeService).hasLiked(1, 10, 7);
        verify(forumLikeService).countLikes(1, 10);
    }

    private String bearerToken(Integer userId, String role) {
        return "Bearer " + jwtUtil.generateToken(userId, "user" + userId, role);
    }
}
