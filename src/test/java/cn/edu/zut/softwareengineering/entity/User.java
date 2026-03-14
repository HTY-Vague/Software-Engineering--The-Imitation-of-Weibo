package cn.edu.zut.softwareengineering.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class User {
    private Integer user_id;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String signature;
    private String role;
    private String status;
    private LocalDateTime create_time;
    private LocalDateTime update_time;
}
