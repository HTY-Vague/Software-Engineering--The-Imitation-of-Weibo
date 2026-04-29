package cn.edu.zut.softwareengineering.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class User {
    private Integer userId;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String signature;
    private String role;
    private String status;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createTime;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime updateTime;
}
