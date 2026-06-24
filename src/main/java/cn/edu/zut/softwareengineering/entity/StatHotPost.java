package cn.edu.zut.softwareengineering.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 热门帖子榜（对应 stat_hot_post 表，由 Spark 跑批写入）。
 */
@Data
public class StatHotPost {
    private Integer rankNum;
    private Long postId;
    private Long boardId;
    private Long authorId;
    private String title;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Double hotScore;
    private LocalDateTime updateTime;
}
