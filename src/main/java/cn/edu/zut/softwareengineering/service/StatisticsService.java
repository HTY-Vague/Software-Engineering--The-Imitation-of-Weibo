package cn.edu.zut.softwareengineering.service;

import cn.edu.zut.softwareengineering.entity.StatActiveUser;
import cn.edu.zut.softwareengineering.entity.StatBoardActive;
import cn.edu.zut.softwareengineering.entity.StatContentQuality;
import cn.edu.zut.softwareengineering.entity.StatDailyTrend;
import cn.edu.zut.softwareengineering.entity.StatHotPost;
import cn.edu.zut.softwareengineering.entity.StatOverview;

import java.util.List;
import java.util.Map;

/**
 * 统计分析服务。
 *
 * 数据来源说明：
 *   - 历史版本由 Service 实时通过 Mapper 聚合业务表计算。
 *   - 当前版本改为读取由 Spark 离线跑批写入的 stat_* 统计表，
 *     降低在线查询压力并作为 Spark 分析能力的展示。
 *   - Spark 程序每日跑批一次（服务器 cron 调度）。
 */
public interface StatisticsService {

    /**
     * 统计概览（用户/板块/帖子/评论/点赞总数）。
     */
    Map<String, Object> getOverview();

    /**
     * 热门帖子榜（按热度分排序）。
     */
    List<StatHotPost> getHotPosts(Integer limit);

    /**
     * 板块活跃度排行。
     */
    List<StatBoardActive> getBoardActive();

    /**
     * 用户活跃度排行。
     */
    List<StatActiveUser> getActiveUsers(Integer limit);

    /**
     * 内容质量分析。
     *
     * @param qualityType 质量分类，null 表示全部；取值：
     *                    HIGH_LIKE_LOW_COMMENT、HIGH_COMMENT_LOW_LIKE、HOT
     */
    List<StatContentQuality> getContentQuality(String qualityType);

    /**
     * 每日趋势（最近 N 天）。
     */
    List<StatDailyTrend> getDailyTrend(Integer days);
}
