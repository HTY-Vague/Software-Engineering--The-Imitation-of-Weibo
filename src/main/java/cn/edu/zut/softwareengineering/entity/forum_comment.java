package cn.edu.zut.softwareengineering.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class forum_comment {
    private Integer comment_id;
    private Integer post_id;
    private Integer user_id;
    private Integer parent_comment_id;
    private Integer reply_comment_id;
    private String content;
    private Integer like_count;
    private String status;
    private LocalDateTime create_time;
    private LocalDateTime update_time;
}
