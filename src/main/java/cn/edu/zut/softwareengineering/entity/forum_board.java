package cn.edu.zut.softwareengineering.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class forum_board {
    private Integer board_id;
    private String board_name;
    private String description;
    private Integer sort_num;
    private String status;
    private LocalDateTime create_time;
    private LocalDateTime update_time;
}
