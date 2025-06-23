package com.oreum.auth.Controller; // 패키지 변경

import com.oreum.auth.dto.FeedbackRequest; // DTO 임포트
import com.oreum.auth.model.Feedback; // Entity 임포트
import com.oreum.auth.mapper.FeedbackRepository; // Repository 임포트
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FeedbackController {

    private final FeedbackRepository feedbackRepository;

    @Autowired
    public FeedbackController(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    @PostMapping("/feedback")
    public ResponseEntity<String> submitFeedback(@Valid @RequestBody FeedbackRequest feedbackRequest) {
        try {
            // DTO에서 Entity로 데이터 복사
            Feedback feedback = new Feedback();
            feedback.setUserId(feedbackRequest.getUserId());
            feedback.setContent(feedbackRequest.getContent());
            // createdAt은 Feedback 엔티티의 @PrePersist에서 자동으로 설정됩니다.

            feedbackRepository.save(feedback); // 데이터베이스에 피드백 저장

            return new ResponseEntity<>("{\"message\": \"피드백이 성공적으로 제출되었습니다.\"}", HttpStatus.CREATED);
        } catch (Exception e) {
            // 오류 발생 시 500 Internal Server Error 반환
            return new ResponseEntity<>("{\"message\": \"피드백 저장 중 오류가 발생했습니다: " + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}