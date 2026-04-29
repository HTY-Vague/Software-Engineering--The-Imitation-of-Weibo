package cn.edu.zut.softwareengineering.service.Impl;

import cn.edu.zut.softwareengineering.entity.ForumLike;
import cn.edu.zut.softwareengineering.mapper.ForumCommentMapper;
import cn.edu.zut.softwareengineering.mapper.ForumLikeMapper;
import cn.edu.zut.softwareengineering.mapper.ForumPostMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForumLikeServiceImplTest {

    @Mock
    private ForumLikeMapper forumLikeMapper;

    @Mock
    private ForumPostMapper forumPostMapper;

    @Mock
    private ForumCommentMapper forumCommentMapper;

    @InjectMocks
    private ForumLikeServiceImpl forumLikeService;

    @Test
    @DisplayName("帖子点赞成功时插入点赞并递增帖子点赞数")
    void likePostSuccess() {
        forumLikeService.like(1, 10, 2);

        verify(forumLikeMapper).insertLike(any(ForumLike.class));
        verify(forumPostMapper).incrementLikeCount(10);
    }

    @Test
    @DisplayName("评论点赞成功时递增评论点赞数")
    void likeCommentSuccess() {
        forumLikeService.like(2, 10, 2);

        verify(forumLikeMapper).insertLike(any(ForumLike.class));
        verify(forumCommentMapper).incrementLikeCount(10);
    }

    @Test
    @DisplayName("重复点赞时抛出状态异常")
    void duplicateLikeThrowsException() {
        ForumLike like = new ForumLike();
        when(forumLikeMapper.getUserLike(1, 10, 2)).thenReturn(like);

        assertThrows(IllegalStateException.class, () -> forumLikeService.like(1, 10, 2));
    }

    @Test
    @DisplayName("取消帖子点赞时删除点赞并递减帖子点赞数")
    void unlikePostSuccess() {
        ForumLike like = new ForumLike();
        when(forumLikeMapper.getUserLike(1, 10, 2)).thenReturn(like);

        forumLikeService.unlike(1, 10, 2);

        verify(forumLikeMapper).deleteLike(1, 10, 2);
        verify(forumPostMapper).decrementLikeCount(10);
    }
}
