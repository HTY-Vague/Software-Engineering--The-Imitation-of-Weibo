package cn.edu.zut.softwareengineering.service.Impl;

import cn.edu.zut.softwareengineering.entity.StatActiveUser;
import cn.edu.zut.softwareengineering.entity.StatBoardActive;
import cn.edu.zut.softwareengineering.entity.StatContentQuality;
import cn.edu.zut.softwareengineering.entity.StatDailyTrend;
import cn.edu.zut.softwareengineering.entity.StatHotPost;
import cn.edu.zut.softwareengineering.entity.StatOverview;
import cn.edu.zut.softwareengineering.mapper.StatMapper;
import cn.edu.zut.softwareengineering.service.StatisticsService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计分析服务实现。
 *
 * 改造说明：
 *   历史版本通过 UserMapper/ForumPostMapper 等多个 Mapper 实时 COUNT 聚合业务表。
 *   当前版本改为读取 Spark 离线跑批写入的 stat_* 统计表，原因：
 *     1. 展示 Spark 大数据分析能力（课程设计要求）。
 *     2. 避免在线请求对业务表做多次全表 COUNT，降低数据库压力。
 *     3. 统计数据容忍日级延迟，不需要实时。
 *
 * 数据来源：StatMapper 读取 stat_overview / stat_hot_post / stat_board_active /
 *           stat_active_user / stat_content_quality / stat_daily_trend 表。
 * 这些表由服务器上 Spark 程序（/opt/forum-stats.jar）每日 03:00 通过 cron 跑批覆盖写入。
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final StatMapper statMapper;

    public StatisticsServiceImpl(StatMapper statMapper) {
        this.statMapper = statMapper;
    }

    @Override
    public Map<String, Object> getOverview() {
        StatOverview stat = statMapper.getOverview();
        Map<String, Object> overview = new LinkedHashMap<>();
        if (stat == null) {
            overview.put("userCount", 0);
            overview.put("activeUserCount", 0);
            overview.put("enabledBoardCount", 0);
            overview.put("postCount", 0);
            overview.put("commentCount", 0);
            overview.put("likeCount", 0);
            overview.put("updateTime", null);
            return overview;
        }
        overview.put("userCount", safe(stat.getUserCount()));
        overview.put("activeUserCount", safe(stat.getActiveUserCount()));
        overview.put("enabledBoardCount", safe(stat.getEnabledBoardCount()));
        overview.put("postCount", safe(stat.getPostCount()));
        overview.put("commentCount", safe(stat.getCommentCount()));
        overview.put("likeCount", safe(stat.getLikeCount()));
        overview.put("updateTime", stat.getUpdateTime());
        return overview;
    }

    @Override
    public List<StatHotPost> getHotPosts(Integer limit) {
        int queryLimit = limit == null || limit < 1 ? 10 : Math.min(limit, 50);
        return statMapper.getHotPosts(queryLimit);
    }

    @Override
    public List<StatBoardActive> getBoardActive() {
        return statMapper.getBoardActive();
    }

    @Override
    public List<StatActiveUser> getActiveUsers(Integer limit) {
        int queryLimit = limit == null || limit < 1 ? 10 : Math.min(limit, 50);
        return statMapper.getActiveUsers(queryLimit);
    }

    @Override
    public List<StatContentQuality> getContentQuality(String qualityType) {
        return statMapper.getContentQuality(qualityType);
    }

    @Override
    public List<StatDailyTrend> getDailyTrend(Integer days) {
        int queryDays = days == null || days < 1 ? 7 : Math.min(days, 90);
        return statMapper.getDailyTrend(queryDays);
    }

    private Integer safe(Integer value) {
        return value == null ? 0 : value;
    }
}
