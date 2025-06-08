package com.oreum.auth.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserRecordDTO {
    private Integer userId;
    private String email;
    private String passwordHash;
    private String name; 
    private String nickname;
    private String profileImage;
    private String address;
    private Integer points;
    private String role; // 'user', 'admin'
    private String status; // 'active', 'inactive', 'banned'
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
}
