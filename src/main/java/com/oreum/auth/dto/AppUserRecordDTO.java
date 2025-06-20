package com.oreum.auth.dto;

import lombok.Data;
import java.time.LocalDateTime;

// 사용자 정보를 담는 DTO (Data Transfer Object)
@Data
public class AppUserRecordDTO {
    private Integer userId; // 사용자 고유 ID
    private String email; // 로그인 이메일
    private String passwordHash; // 비밀번호 해시
    private String name; // 실명
    private String nickname; // 닉네임
    private String profileImage; // 프로필 이미지 URL
    private String address; // 사용자 주소
    private Integer points; // 포인트
    private String role; // 권한 (user, admin)
    private String status; // 계정 상태 (active, inactive, banned)
    private LocalDateTime lastLogin; // 마지막 로그인 시각
    private LocalDateTime createdAt; // 가입 시각
}
