package cn.edu.zut.softwareengineering.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 内容质量分析（对应 stat_content_quality 表，由 Spark 跑批写入）。
 * quality_type 取值：HIGH_LIKE_LOW_COMMENT / HIGH_COMMENT_LOW_LIKE / HOT
 */
@Data
public class StatContentQuality {
    private Long postId;
    private String title;
    private Integer likeCount;
    private Integer commentCount;
    private Integer viewCount;
    private String qualityType;
    private LocalDateTime updateTime;
}
