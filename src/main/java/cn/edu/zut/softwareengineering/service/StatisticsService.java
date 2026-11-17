package cn.edu.zut.softwareengineering.service;

import java.util.Map;

/**
 * 统计分析服务。
 *
 * 数据来源说明：
 *   - 历史版本读取 Spark 离线跑批写入的 stat_* 统计表。
 *   - 当前版本移除 Spark 链路后，改为对业务表实时 COUNT 聚合，
 *     数据随业务写操作即时更新，不存在批处理延迟。
 */
public interface StatisticsService {

    /**
     * 统计概览（用户/板块/帖子/评论/点赞总数），实时聚合业务表。
     */
    Map<String, Object> getOverview();
}
