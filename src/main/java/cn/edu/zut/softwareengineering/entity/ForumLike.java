package cn.edu.zut.softwareengineering.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ForumLike {
    private Integer likeId;
    private Integer postId;
    private Integer userId;
    private LocalDateTime createTime;
}
