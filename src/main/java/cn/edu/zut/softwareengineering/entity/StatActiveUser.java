package cn.edu.zut.softwareengineering.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户活跃度排行（对应 stat_active_user 表，由 Spark 跑批写入）。
 */
@Data
public class StatActiveUser {
    private Integer rankNum;
    private Long userId;
    private String username;
    private String nickname;
    private Integer postCount;
    private Integer commentCount;
    private Integer likeCount;
    private Double activeScore;
    private LocalDateTime updateTime;
}
