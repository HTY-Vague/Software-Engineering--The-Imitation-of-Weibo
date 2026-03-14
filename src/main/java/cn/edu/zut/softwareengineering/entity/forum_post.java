package cn.edu.zut.softwareengineering.entity;

import java.time.LocalDateTime;

import lombok.Data;
@Data
public class forum_post {
    private Integer post_id;
    private Integer board_id;
    private Integer author_id;
    private String title;
    private String content;
    private Integer view_count;
    private Integer like_id;
    private Integer comment_count;
    private Integer is_top;
    private String status;
    private LocalDateTime create_time;
    private LocalDateTime update_time;
}
