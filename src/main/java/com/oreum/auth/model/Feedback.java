package com.oreum.auth.model; // 패키지 변경

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks") // 데이터베이스 테이블 이름과 매핑
@Data // Lombok: Getter, Setter, toString, equals, hashCode 자동 생성
@NoArgsConstructor // Lombok: 기본 생성자 자동 생성
@AllArgsConstructor // Lombok: 모든 필드를 포함하는 생성자 자동 생성
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    @Column(name = "feedback_id")
    private Long feedbackId;

    @Column(name = "user_id", nullable = false)
    private Long userId; // 유저 아이디

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content; // 피드백 내용

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 글 작성일시

    @PrePersist // 엔티티가 영속화되기 전에 실행 (삽입 전)
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}