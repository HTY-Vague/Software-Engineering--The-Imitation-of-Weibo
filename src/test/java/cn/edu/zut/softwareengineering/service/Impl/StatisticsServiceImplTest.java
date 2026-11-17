package cn.edu.zut.softwareengineering.service.Impl;

import cn.edu.zut.softwareengineering.mapper.ForumBoardMapper;
import cn.edu.zut.softwareengineering.mapper.ForumCommentMapper;
import cn.edu.zut.softwareengineering.mapper.ForumLikeMapper;
import cn.edu.zut.softwareengineering.mapper.ForumPostMapper;
import cn.edu.zut.softwareengineering.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 统计概览实时 COUNT 聚合测试（P0 移除 Spark 链路后的新测试）。
 */
@ExtendWith(MockitoExtension.class)
class StatisticsServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private ForumBoardMapper boardMapper;

    @Mock
    private ForumPostMapper postMapper;

    @Mock
    private ForumCommentMapper commentMapper;

    @Mock
    private ForumLikeMapper likeMapper;

    private StatisticsServiceImpl statisticsService;

    @BeforeEach
    void setUp() {
        statisticsService = new StatisticsServiceImpl(
                userMapper, boardMapper, postMapper, commentMapper, likeMapper);
    }

    @Test
    @DisplayName("统计概览对业务表实时 COUNT 聚合，字段与结构保持不变")
    void getOverviewAggregatesCountsFromBusinessTables() {
        when(userMapper.countUsers()).thenReturn(42);
        when(userMapper.countActiveUsers()).thenReturn(40);
        when(boardMapper.countEnabledBoards()).thenReturn(6);
        when(postMapper.countPosts()).thenReturn(100);
        when(commentMapper.countComments()).thenReturn(500);
        when(likeMapper.countLikes()).thenReturn(800);

        Map<String, Object> overview = statisticsService.getOverview();

        assertEquals(42, overview.get("userCount"));
        assertEquals(40, overview.get("activeUserCount"));
        assertEquals(6, overview.get("enabledBoardCount"));
        assertEquals(100, overview.get("postCount"));
        assertEquals(500, overview.get("commentCount"));
        assertEquals(800, overview.get("likeCount"));
        assertNotNull(overview.get("updateTime"));

        verify(userMapper).countUsers();
        verify(userMapper).countActiveUsers();
        verify(boardMapper).countEnabledBoards();
        verify(postMapper).countPosts();
        verify(commentMapper).countComments();
        verify(likeMapper).countLikes();
    }

    @Test
    @DisplayName("COUNT 结果为 null 时各计数字段兜底为 0")
    void getOverviewHandlesNullCounts() {
        when(userMapper.countUsers()).thenReturn(null);
        when(userMapper.countActiveUsers()).thenReturn(null);
        when(boardMapper.countEnabledBoards()).thenReturn(null);
        when(postMapper.countPosts()).thenReturn(null);
        when(commentMapper.countComments()).thenReturn(null);
        when(likeMapper.countLikes()).thenReturn(null);

        Map<String, Object> overview = statisticsService.getOverview();

        assertEquals(0, overview.get("userCount"));
        assertEquals(0, overview.get("activeUserCount"));
        assertEquals(0, overview.get("enabledBoardCount"));
        assertEquals(0, overview.get("postCount"));
        assertEquals(0, overview.get("commentCount"));
        assertEquals(0, overview.get("likeCount"));
        assertNotNull(overview.get("updateTime"));
    }
}
