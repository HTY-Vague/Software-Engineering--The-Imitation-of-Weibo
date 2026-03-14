package cn.edu.zut.softwareengineering.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class forum_like {
    private Integer like_id;
    private Integer post_id;
    private Integer user_id;
    private LocalDateTime create_time;
}
