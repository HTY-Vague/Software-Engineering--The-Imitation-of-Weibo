package cn.edu.zut.softwareengineering.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 板块活跃度统计（对应 stat_board_active 表，由 Spark 跑批写入）。
 */
@Data
public class StatBoardActive {
    private Long boardId;
    private String boardName;
    private Integer postCount;
    private Long viewSum;
    private Integer likeSum;
    private Integer commentSum;
    private Double activeScore;
    private LocalDateTime updateTime;
}
