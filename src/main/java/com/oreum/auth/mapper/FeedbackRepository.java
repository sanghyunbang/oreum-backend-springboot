package com.oreum.auth.mapper; // 패키지 변경

import com.oreum.auth.model.Feedback; // Feedback 엔티티 임포트
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    // JpaRepository가 Feedback 엔티티에 대한 기본적인 CRUD 메서드를 자동으로 제공합니다.
    // 추가적인 메서드가 필요하지 않으므로, 내용은 비워둡니다.
}