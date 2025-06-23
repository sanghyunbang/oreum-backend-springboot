package com.oreum.auth.dto; // 패키지 변경

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeedbackRequest {
    @NotNull(message = "userId는 필수입니다.")
    private Long userId;

    @NotBlank(message = "content는 필수입니다.")
    private String content;
}