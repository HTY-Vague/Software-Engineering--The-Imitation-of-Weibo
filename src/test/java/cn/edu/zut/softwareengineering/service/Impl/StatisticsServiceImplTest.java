package cn.edu.zut.softwareengineering.service.Impl;

import cn.edu.zut.softwareengineering.entity.StatActiveUser;
import cn.edu.zut.softwareengineering.entity.StatBoardActive;
import cn.edu.zut.softwareengineering.entity.StatContentQuality;
import cn.edu.zut.softwareengineering.entity.StatDailyTrend;
import cn.edu.zut.softwareengineering.entity.StatHotPost;
import cn.edu.zut.softwareengineering.entity.StatOverview;
import cn.edu.zut.softwareengineering.mapper.StatMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceImplTest {

    @Mock
    private StatMapper statMapper;

    @InjectMocks
    private StatisticsServiceImpl statisticsService;

    @Test
    @DisplayName("统计概览读取 Spark 写入的 stat_overview 快照")
    void getOverviewSuccess() {
        StatOverview stat = new StatOverview();
        stat.setId(1L);
        stat.setUserCount(10);
        stat.setActiveUserCount(8);
        stat.setEnabledBoardCount(3);
        stat.setPostCount(20);
        stat.setCommentCount(50);
        stat.setLikeCount(100);
        stat.setUpdateTime(LocalDateTime.now());
        when(statMapper.getOverview()).thenReturn(stat);

        Map<String, Object> overview = statisticsService.getOverview();

        assertEquals(10, overview.get("userCount"));
        assertEquals(8, overview.get("activeUserCount"));
        assertEquals(3, overview.get("enabledBoardCount"));
        assertEquals(20, overview.get("postCount"));
        assertEquals(50, overview.get("commentCount"));
        assertEquals(100, overview.get("likeCount"));
    }

    @Test
    @DisplayName("统计概览在快照不存在时返回零值")
    void getOverviewEmptyFallback() {
        when(statMapper.getOverview()).thenReturn(null);

        Map<String, Object> overview = statisticsService.getOverview();

        assertEquals(0, overview.get("userCount"));
        assertEquals(0, overview.get("postCount"));
    }

    @Test
    @DisplayName("热门帖子默认查询10条")
    void getHotPostsUsesDefaultLimit() {
        List<StatHotPost> posts = List.of(new StatHotPost());
        when(statMapper.getHotPosts(10)).thenReturn(posts);

        List<StatHotPost> result = statisticsService.getHotPosts(null);

        assertEquals(posts, result);
        verify(statMapper).getHotPosts(10);
    }

    @Test
    @DisplayName("热门帖子最多查询50条")
    void getHotPostsCapsLimit() {
        statisticsService.getHotPosts(100);

        verify(statMapper).getHotPosts(50);
    }

    @Test
    @DisplayName("板块活跃度直接读取统计表")
    void getBoardActive() {
        when(statMapper.getBoardActive()).thenReturn(List.of(new StatBoardActive()));
        assertEquals(1, statisticsService.getBoardActive().size());
    }

    @Test
    @DisplayName("用户活跃度排行默认10条，最多50条")
    void getActiveUsersLimit() {
        when(statMapper.getActiveUsers(10)).thenReturn(List.of(new StatActiveUser()));
        statisticsService.getActiveUsers(null);
        verify(statMapper).getActiveUsers(10);

        statisticsService.getActiveUsers(200);
        verify(statMapper).getActiveUsers(50);
    }

    @Test
    @DisplayName("内容质量分析支持按类型过滤")
    void getContentQualityByType() {
        when(statMapper.getContentQuality("HOT")).thenReturn(List.of(new StatContentQuality()));
        statisticsService.getContentQuality("HOT");
        verify(statMapper).getContentQuality("HOT");
    }

    @Test
    @DisplayName("每日趋势默认7天，最多90天")
    void getDailyTrendDays() {
        when(statMapper.getDailyTrend(7)).thenReturn(List.of(new StatDailyTrend()));
        statisticsService.getDailyTrend(null);
        verify(statMapper).getDailyTrend(7);

        statisticsService.getDailyTrend(365);
        verify(statMapper).getDailyTrend(90);
    }
}
