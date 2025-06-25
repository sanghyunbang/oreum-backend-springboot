package com.oreum.posts.dto;

import lombok.Data;

@Data
public class PostForCurationDTO {
    private int userId;             // 작성자 ID [이건 DB 넣는데 활용하는거고]
    private String nickname;        // 홈페이지 내 유저 정체성 확인용!!!
    private int boardId;            // 소속 커뮤니티 ID
    private String type;            // 게시글 유형 (general, curation, meeting)
    private String title;           // 제목
    private String mountainName;    // [큐레이션 관련 추가]
}
