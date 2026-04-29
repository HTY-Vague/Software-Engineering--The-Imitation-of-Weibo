package cn.edu.zut.softwareengineering.service.Impl;

import cn.edu.zut.softwareengineering.entity.ForumPost;
import cn.edu.zut.softwareengineering.mapper.ForumCommentMapper;
import cn.edu.zut.softwareengineering.mapper.ForumLikeMapper;
import cn.edu.zut.softwareengineering.mapper.ForumPostMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForumPostServiceImplTest {

    @Mock
    private ForumPostMapper forumPostMapper;

    @Mock
    private ForumLikeMapper forumLikeMapper;

    @Mock
    private ForumCommentMapper forumCommentMapper;

    @InjectMocks
    private ForumPostServiceImpl forumPostService;

    @Test
    @DisplayName("发布帖子成功时写入帖子并返回原对象")
    void createPostSuccess() {
        ForumPost post = new ForumPost();
        post.setBoardId(1);
        post.setAuthorId(2);
        post.setTitle("测试标题");
        post.setContent("测试内容");

        ForumPost result = forumPostService.createPost(post);

        assertEquals(post, result);
        verify(forumPostMapper).insertPost(post);
    }

    @Test
    @DisplayName("发布帖子缺少标题时抛出参数异常")
    void createPostWithoutTitleThrowsException() {
        ForumPost post = new ForumPost();
        post.setBoardId(1);
        post.setAuthorId(2);
        post.setContent("测试内容");

        assertThrows(IllegalArgumentException.class, () -> forumPostService.createPost(post));
    }

    @Test
    @DisplayName("查询帖子详情时先递增浏览量再查询详情")
    void getPostDetailIncrementsViewCount() {
        ForumPost post = new ForumPost();
        post.setPostId(1);
        when(forumPostMapper.getPostById(1)).thenReturn(post);

        ForumPost result = forumPostService.getPostDetail(1);

        assertEquals(post, result);
        verify(forumPostMapper).incrementViewCount(1);
        verify(forumPostMapper).getPostById(1);
    }

    @Test
    @DisplayName("删除帖子时软删除帖子、清理帖子点赞和评论点赞、软删除评论")
    void deletePostDeletesLikesAndComments() {
        forumPostService.deletePost(1);

        verify(forumPostMapper).deletePost(1);
        verify(forumLikeMapper).deleteByTarget(1, 1);
        verify(forumLikeMapper).deleteCommentLikesByPostId(1);
        verify(forumCommentMapper).deleteCommentsByPostId(1);
    }

    @Test
    @DisplayName("作者删除自己的帖子时允许删除并清理评论和点赞")
    void deleteOwnPostSuccess() {
        ForumPost post = new ForumPost();
        post.setPostId(1);
        post.setAuthorId(2);
        when(forumPostMapper.getPostById(1)).thenReturn(post);

        forumPostService.deleteOwnPost(1, 2);

        verify(forumPostMapper).deletePost(1);
        verify(forumLikeMapper).deleteByTarget(1, 1);
        verify(forumLikeMapper).deleteCommentLikesByPostId(1);
        verify(forumCommentMapper).deleteCommentsByPostId(1);
    }

    @Test
    @DisplayName("非作者删除帖子时抛出状态异常")
    void deleteOwnPostByOtherUserThrowsException() {
        ForumPost post = new ForumPost();
        post.setPostId(1);
        post.setAuthorId(2);
        when(forumPostMapper.getPostById(1)).thenReturn(post);

        assertThrows(IllegalStateException.class, () -> forumPostService.deleteOwnPost(1, 3));
    }

    @Test
    @DisplayName("作者更新自己的帖子时允许更新")
    void updateOwnPostSuccess() {
        ForumPost existing = new ForumPost();
        existing.setPostId(1);
        existing.setAuthorId(2);
        when(forumPostMapper.getPostById(1)).thenReturn(existing);

        ForumPost update = new ForumPost();
        update.setPostId(1);
        update.setTitle("新标题");
        update.setContent("新内容");

        forumPostService.updateOwnPost(update, 2);

        verify(forumPostMapper).updatePost(update);
    }
}
