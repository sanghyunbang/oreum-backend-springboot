package com.oreum.auth.dto;

import lombok.Data;

@Data
public class UserDTO {
    private String role;    // 기본 "user"
    private String name;  // OAuth에서 받은 사용자 이름 (예: 홍길동)
    private String username; // OAuth에서 받은 이메일
    
}
