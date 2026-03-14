package cn.edu.zut.softwareengineering.entity;

import java.time.LocalDateTime;

import lombok.Data;
@Data
public class ForumPost {
    private Integer postId;
    private Integer boardId;
    private Integer authorId;
    private String title;
    private String content;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer isTop;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
