package cn.edu.zut.softwareengineering.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ForumComment {
    private Integer commentId;
    private Integer postId;
    private Integer userId;
    private Integer parentCommentId;
    private Integer replyCommentId;
    private String content;
    private Integer likeCount;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
