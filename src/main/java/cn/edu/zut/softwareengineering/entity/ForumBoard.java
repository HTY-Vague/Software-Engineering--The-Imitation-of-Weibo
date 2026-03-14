package cn.edu.zut.softwareengineering.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ForumBoard {
    private Integer boardId;
    private String boardName;
    private String description;
    private Integer sortNum;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
