package com.oreum.posts.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostsDTO {
    private int postId;             // 게시글 고유 ID
    private int userId;             // 작성자 ID [이건 DB 넣는데 활용하는거고]
    private String nickname;        // 홈페이지 내 유저 정체성 확인용!!!
    private int boardId;            // 소속 커뮤니티 ID
    private String type;            // 게시글 유형 (general, curation, meeting)
    private String title;           // 제목
    private String content;         // 본문 내용

    // private String mountainName;    // [큐레이션 관련 추가]
    private String route;
    private String caution;
    private String nearbyAttraction;

    private int likeCount;          // 좋아요 수
    private int commentCount;       // 댓글 수
    private boolean isDeleted;      // 삭제 여부
    private LocalDateTime createdAt;// 작성일
    private LocalDateTime updatedAt;// 수정일
    
    								 //[모임관련]
    private String meetingDate;       // YYYY-MM-DD 형식
    private String meetingLocation;

    private List<MediaDTO> mediaList; // S3 업로드 결과 저장용 필드
    private List<CommentDTO> comments;

    // private Integer curationId; // 큐레이션 글 작성 관련  
    // private boolean isUpward; // 큐레이션 글 관련

    // 몽고 DB 큐레이션 정보
    private List<CurationSegmentDoc> curationSegments;

    // 대표 좌표
    private String searchGeo;
}
