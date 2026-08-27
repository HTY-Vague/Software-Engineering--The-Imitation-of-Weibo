package cn.edu.zut.softwareengineering.entity;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class ForumComment {
    private Integer commentId;
    private Integer postId;
    private Integer userId;
    private Integer parentCommentId;
    private Integer replyUserId;
    private String content;
    private Integer likeCount;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<ForumComment> children;

    private String username;
    private String nickname;
    private String replyUsername;
    private String replyNickname;
}
