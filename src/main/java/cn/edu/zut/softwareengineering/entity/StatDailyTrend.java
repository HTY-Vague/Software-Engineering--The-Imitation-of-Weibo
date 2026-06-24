package cn.edu.zut.softwareengineering.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日趋势统计（对应 stat_daily_trend 表，由 Spark 跑批写入）。
 */
@Data
public class StatDailyTrend {
    private LocalDate statDate;
    private Integer newUserCount;
    private Integer newPostCount;
    private Integer newCommentCount;
    private Integer newLikeCount;
    private LocalDateTime updateTime;
}
