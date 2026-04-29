package cn.edu.zut.softwareengineering.service.Impl;

import cn.edu.zut.softwareengineering.entity.ForumPost;
import cn.edu.zut.softwareengineering.mapper.ForumBoardMapper;
import cn.edu.zut.softwareengineering.mapper.ForumCommentMapper;
import cn.edu.zut.softwareengineering.mapper.ForumLikeMapper;
import cn.edu.zut.softwareengineering.mapper.ForumPostMapper;
import cn.edu.zut.softwareengineering.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private ForumBoardMapper forumBoardMapper;

    @Mock
    private ForumPostMapper forumPostMapper;

    @Mock
    private ForumCommentMapper forumCommentMapper;

    @Mock
    private ForumLikeMapper forumLikeMapper;

    @InjectMocks
    private StatisticsServiceImpl statisticsService;

    @Test
    @DisplayName("统计概览聚合用户、板块、帖子、评论和点赞数量")
    void getOverviewSuccess() {
        when(userMapper.countUsers()).thenReturn(10);
        when(userMapper.countActiveUsers()).thenReturn(8);
        when(forumBoardMapper.countEnabledBoards()).thenReturn(3);
        when(forumPostMapper.countPosts()).thenReturn(20);
        when(forumCommentMapper.countComments()).thenReturn(50);
        when(forumLikeMapper.countLikes()).thenReturn(100);

        Map<String, Object> overview = statisticsService.getOverview();

        assertEquals(10, overview.get("userCount"));
        assertEquals(8, overview.get("activeUserCount"));
        assertEquals(3, overview.get("enabledBoardCount"));
        assertEquals(20, overview.get("postCount"));
        assertEquals(50, overview.get("commentCount"));
        assertEquals(100, overview.get("likeCount"));
    }

    @Test
    @DisplayName("热门帖子默认查询10条")
    void getHotPostsUsesDefaultLimit() {
        List<ForumPost> posts = List.of(new ForumPost());
        when(forumPostMapper.getHotPosts(10)).thenReturn(posts);

        List<ForumPost> result = statisticsService.getHotPosts(null);

        assertEquals(posts, result);
        verify(forumPostMapper).getHotPosts(10);
    }

    @Test
    @DisplayName("热门帖子最多查询50条")
    void getHotPostsCapsLimit() {
        statisticsService.getHotPosts(100);

        verify(forumPostMapper).getHotPosts(50);
    }
}
