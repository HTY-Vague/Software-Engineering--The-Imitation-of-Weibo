package cn.edu.zut.softwareengineering.service.Impl;

import cn.edu.zut.softwareengineering.entity.ForumComment;
import cn.edu.zut.softwareengineering.mapper.ForumCommentMapper;
import cn.edu.zut.softwareengineering.mapper.ForumLikeMapper;
import cn.edu.zut.softwareengineering.mapper.ForumPostMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForumCommentServiceImplTest {

    @Mock
    private ForumCommentMapper forumCommentMapper;

    @Mock
    private ForumPostMapper forumPostMapper;

    @Mock
    private ForumLikeMapper forumLikeMapper;

    @InjectMocks
    private ForumCommentServiceImpl forumCommentService;

    @Test
    @DisplayName("发表评论成功时写入评论并递增帖子评论数")
    void createCommentSuccess() {
        ForumComment comment = new ForumComment();
        comment.setPostId(1);
        comment.setUserId(2);
        comment.setContent("测试评论");

        ForumComment result = forumCommentService.createComment(comment);

        assertEquals(comment, result);
        verify(forumCommentMapper).insertComment(comment);
        verify(forumPostMapper).incrementCommentCount(1);
    }

    @Test
    @DisplayName("评论内容为空时抛出参数异常")
    void createCommentWithoutContentThrowsException() {
        ForumComment comment = new ForumComment();
        comment.setPostId(1);
        comment.setUserId(2);

        assertThrows(IllegalArgumentException.class, () -> forumCommentService.createComment(comment));
    }

    @Test
    @DisplayName("评论列表按父子关系组装两级结构")
    void getCommentsBuildsTree() {
        ForumComment root = new ForumComment();
        root.setCommentId(1);
        root.setPostId(10);

        ForumComment child = new ForumComment();
        child.setCommentId(2);
        child.setPostId(10);
        child.setParentCommentId(1);

        when(forumCommentMapper.getCommentsByPostId(10)).thenReturn(List.of(root, child));

        List<ForumComment> result = forumCommentService.getComments(10);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getChildren().size());
        assertEquals(2, result.get(0).getChildren().get(0).getCommentId());
    }

    @Test
    @DisplayName("删除评论时软删除评论、清理点赞并递减帖子评论数")
    void deleteCommentSuccess() {
        ForumComment comment = new ForumComment();
        comment.setCommentId(3);
        comment.setPostId(10);
        when(forumCommentMapper.getCommentById(3)).thenReturn(comment);

        forumCommentService.deleteComment(3);

        verify(forumCommentMapper).deleteComment(3);
        verify(forumLikeMapper).deleteByTarget(2, 3);
        verify(forumPostMapper).decrementCommentCount(10);
    }
}
