package com.oreum.posts.controller;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.oreum.external.S3.S3Service;
import com.oreum.posts.dao.PostsDAO;
import com.oreum.posts.dto.BookmarkDTO;
import com.oreum.posts.dto.CommentDTO;
import com.oreum.posts.dto.PostLikeDTO;
import com.oreum.posts.dto.PostsDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true") //  CORS 허용
public class postController {
	@Autowired PostsDAO pd;

    private final S3Service s3Service;

    @PostMapping(value = "/insert", consumes = "multipart/form-data")
    public ResponseEntity<?> insertPost(@RequestPart("post") PostsDTO postDTO,
                                        @RequestPart(value = "media", required = false)
                                        List<MultipartFile> mediaFiles) {
    	
    	System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ게시글 등록 진입ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
    	System.out.println("	작성자 : " + postDTO.getNickname()
    						+"\n	보드 id :" + postDTO.getBoardId()
    						+"\n	제목 : " + postDTO.getTitle());

        // DB 저장하기 1 (게시글 공통 테이블)
        pd.insertpost(postDTO);

        // DB 저장하기 2 (큐레이션 여부 따져서 큐레이션 저장)
        if("curation".equals(postDTO.getType())){
            pd.insertCurationDetail(postDTO);
        } else if ("meeting".equals(postDTO.getType())) {
            pd.insertMeetingDetail(postDTO);
        }
        // S3에 업로드 하고 URL 받아서 저장
        if (mediaFiles != null && !mediaFiles.isEmpty()){
            for (MultipartFile file : mediaFiles) { 
                String url = s3Service.uploadFile(file);
                
                String mediaType = file.getContentType().startsWith("image") ? "image" : "video";

                pd.insertPostMedia(postDTO.getPostId(), mediaType, url);
            }
        }        
        return ResponseEntity.ok("게시글 등록 완료");
    }
    
    @GetMapping("/list")
    public ResponseEntity<?> getAllPosts() {
    	System.out.println("		게시물 리스트 불러오기");
        try {
            List<PostsDTO> posts = pd.getAllPosts();
            
            for (PostsDTO post : posts) {
                int postId = post.getPostId();

                int count = pd.countComments(postId);
                post.setCommentCount(count); // 댓글 수 채워 넣기
                
                List<CommentDTO> comments = pd.getCommentsByPostId(postId);
                post.setComments(comments);

                //미디어 관련
                post.setMediaList(pd.getPostMedia(postId));
            }

            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("게시글 목록 불러오기 실패");
        }
    }
    
    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostDetail(@PathVariable("postId") String postIdstr) {
    	System.out.println("		포스트 상세보기 진입 ID : "+postIdstr);
        try {
        	int postId = Integer.parseInt(postIdstr);
            PostsDTO post = pd.getPostById(postId);
            System.out.println(post);
            if (post == null) {
                return ResponseEntity.notFound().build();
            }

            // 댓글 수 포함
            int commentCount = pd.countComments(postId);
            post.setCommentCount(commentCount);
            
            List<CommentDTO> comments = pd.getCommentsByPostId(postId);
            post.setComments(comments);

            //미디어 추가
            post.setMediaList(pd.getPostMedia(postId));
            
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("게시글 불러오기 실패");
        }
    }
    
    @PostMapping("/comments")
    public ResponseEntity<?> addComment(@RequestBody CommentDTO comment) {
    	System.out.println("                               게시글 덧글달기 진입");
    	System.out.println("		내용 : " + comment.getContent()
    						+"\n 		포스트 id : " + comment.getPostId()
    						+"\n 		유저 id : " + comment.getUserId());
    	try {
            // postId를 path에서 받고, 댓글 DTO에도 넣어줌
            comment.setCreatedAt(LocalDateTime.now());
            
            if (comment.getUserId() == 0) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            }

            pd.insertComment(comment); // DAO에 댓글 저장

            return ResponseEntity.ok(comment); // 저장된 댓글 정보 반환
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 등록 실패");
        }
    }
    
    @PostMapping("/like")
    public ResponseEntity<?> likePost(@RequestBody PostLikeDTO likeDTO) {
    	System.out.println("			좋아요 요청: postId=" + likeDTO.getPostId() + ", userId=" + likeDTO.getUserId());
    	
    	try {
            PostLikeDTO existingLike = pd.getPostLike(likeDTO.getPostId(), likeDTO.getUserId());
            System.out.println("getpostlike 리턴받은 값 = "+ existingLike);
            if (existingLike != null) {
                // 이미 좋아요 누름 → 좋아요 취소
                pd.deletePostLike(likeDTO.getPostId(), likeDTO.getUserId());
                pd.decrementPostLikeCount(likeDTO.getPostId());
                return ResponseEntity.ok(Map.of("liked", false));
            } else {
                // 좋아요 안 누름 → 좋아요 추가
                likeDTO.setCreatedAt(LocalDateTime.now());
                pd.insertPostLike(likeDTO);
                pd.incrementPostLikeCount(likeDTO.getPostId());
                return ResponseEntity.ok(Map.of("liked", true));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("좋아요 처리 실패");
        }
        
    }

    
    @PostMapping("/bookmark")
    public ResponseEntity<?> toggleBookmark(@RequestBody BookmarkDTO bookmarkDTO) {
    	int userId = bookmarkDTO.getUserId();
        int postId = bookmarkDTO.getPostId();

        System.out.println("		북마크 토글 요청: postId=" + postId + ", userId=" + userId);

        try {
            BookmarkDTO existingBookmark = pd.getBookmark(postId, userId);
            System.out.println("                 북마크 데이터 겟 체크 : "+existingBookmark);
            if (existingBookmark != null) {
                // 북마크 존재 → 삭제
                pd.deleteBookmark(postId, userId);
                return ResponseEntity.ok(Map.of("bookmarked", false));
            } else {
                // 북마크 없음 → 추가
                bookmarkDTO.setCreatedAt(LocalDateTime.now());
                pd.insertBookmark(bookmarkDTO);
                return ResponseEntity.ok(Map.of("bookmarked", true));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("북마크 처리 실패");
        }
    }

    @GetMapping("/bookmarks/{userId}")
    public ResponseEntity<?> getUserBookmarks(@PathVariable("userId") int userId) {
    	
    	System.out.println("	북마크 목록 요청: userId=" + userId);
        try {
            List<Integer> bookmarkedPostIds = pd.getBookmarkedPostIdsByUser(userId);
            return ResponseEntity.ok(bookmarkedPostIds);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("북마크 조회 실패");
        }
    }

    @GetMapping("/{postId}/bookmarked")
    public ResponseEntity<?> isPostBookmarked(
            @PathVariable("postId") int postId,
            @RequestParam("userId") int userId) {

        System.out.println("		북마크 여부 확인 요청: postId=" + postId + ", userId=" + userId);
        
        try {
            BookmarkDTO bookmark = pd.getBookmark(postId, userId);
            boolean bookmarked = (bookmark != null);
            return ResponseEntity.ok(Map.of("bookmarked", bookmarked));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("북마크 여부 확인 실패");
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getPostsByUser(@PathVariable("userId") int userId) {
    	System.out.println("                               사용자가 쓴 글 조회");
        try {
            List<PostsDTO> posts = pd.getPostsByUserId(userId);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("내가 쓴 글 조회 실패");
        }
    }
    
    @GetMapping("/comments/user/{userId}")
    public ResponseEntity<?> getCommentsByUser(@PathVariable("userId") int userId) {
    	System.out.println("                                 사용자가 쓴 댓글＃ 조회");
        try {
            List<CommentDTO> comments = pd.getCommentsByUserId(userId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("내가 쓴 댓글 조회 실패");
        }
    }
    
    @GetMapping("/likes/user/{userId}")
    public ResponseEntity<?> getLikedPosts(@PathVariable("userId") int userId) {
    	System.out.println("                                  사용자가 좋아요 누른 게시물 조회"
    						+ "\n 유저 id : " + userId);
        try {
            List<Integer> likedPostIds = pd.getLikedPostIdsByUser(userId);
            List<PostsDTO> likedPosts = new ArrayList<>();
            for (int postId : likedPostIds) {
                PostsDTO post = pd.getPostById(postId);
                if (post != null) likedPosts.add(post);
            }
            return ResponseEntity.ok(likedPosts);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("좋아요한 게시물 조회 실패");
        }
    }

}
