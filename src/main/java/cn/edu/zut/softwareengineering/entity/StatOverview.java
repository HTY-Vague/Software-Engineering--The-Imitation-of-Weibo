package cn.edu.zut.softwareengineering.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 统计概览快照（对应 stat_overview 表，由 Spark 跑批写入）。
 */
@Data
public class StatOverview {
    private Long id;
    private Integer userCount;
    private Integer activeUserCount;
    private Integer enabledBoardCount;
    private Integer postCount;
    private Integer commentCount;
    private Integer likeCount;
    private LocalDateTime updateTime;
}
