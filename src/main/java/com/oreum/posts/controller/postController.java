package com.oreum.posts.controller;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.oreum.external.S3.S3Service;
import com.oreum.posts.dao.CurationSegmentRepository;
import com.oreum.posts.dao.PostsDAO;
import com.oreum.posts.dto.BookmarkDTO;
import com.oreum.posts.dto.CommentDTO;
import com.oreum.posts.dto.CurationSegmentDoc;
import com.oreum.posts.dto.PostForCurationDTO;
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

    @Autowired
    private CurationSegmentRepository mongoRepo; // mongoDB관련


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

        // Integer generatedCurationId = null; // 큐레이션인 경우 생성

        // DB 저장하기 2 (큐레이션 여부 따져서 큐레이션 저장)
        // if("curation".equals(postDTO.getType())){
        //     pd.insertCurationDetail(postDTO);
        //     // generatedCurationId = postDTO.getCurationId();
        // } else 
        
        if ("meeting".equals(postDTO.getType())) {
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
        // 클라이언트에 보낼 데이터 구성
        Map<String, Object> response = new HashMap<>();
        response.put("message", "게시글 등록 완료");
        response.put("postId", postDTO.getPostId());

        // if (generatedCurationId != null) {
        //     System.out.println(" 생성된 curationId: " + generatedCurationId);
        //     response.put("curationId", generatedCurationId);
        // }

        return ResponseEntity.ok(response);
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
    
    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(@PathVariable("postId") String postId,
                                        @RequestBody PostsDTO updatedPost) {
    	System.out.println("                           게시글 수정 진입 ID :" + postId);
    	int postid = Integer.parseInt(postId);
        try {
            // 게시글 존재 여부 확인
            PostsDTO existingPost = pd.getPostById(postid);
                        
            if (existingPost == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("게시글을 찾을 수 없습니다.");
            }

            // 수정 권한 확인 (작성자만 가능)
            if (existingPost.getUserId() != updatedPost.getUserId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("수정 권한이 없습니다.");
            }

	        // 수정 처리
	        updatedPost.setPostId(postid); // ID를 다시 설정해줌
	        pd.updatePost(updatedPost);
	
	        return ResponseEntity.ok("게시글이 성공적으로 수정되었습니다.");
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시글 수정 실패");
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
            String nickname = pd.getNicknameByUserId(comment.getUserId()); // 👈 새로 추가할 DAO
            comment.setNickname(nickname);

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
    						+ "\n                                  유저 id : " + userId);
        try {
            List<Integer> likedPostIds = pd.getLikedPostIdsByUser(userId);
            System.out.println("                                   likedPostIds 값 확인 : "+likedPostIds);
            
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
    
    @GetMapping("/bookmarks/user/{userId}")
    public ResponseEntity<?> getBookmarks(@PathVariable("userId") int userId){
    	System.out.println("                        북마크 게시물 조회");
    	try {
    		List<Integer> bookmarkIds = pd.getbookmarkIdsByUser(userId);
    		System.out.println("                                   bookmarkId 값 확인 : " +  bookmarkIds);
    				
    			List<PostsDTO> bookmarks = new ArrayList<>();
    		for(int postId : bookmarkIds) {
    			PostsDTO post = pd.getPostById(postId);
    				if(post != null) bookmarks.add(post);
    		}
    		return ResponseEntity.ok(bookmarks);
    	}catch(Exception e){
    		return ResponseEntity.internalServerError().body("북마크 게시물 조회 실패");
    	}

    }
    // 앱  추가: 특정 커뮤니티의 게시글 목록 조회
    @GetMapping("/community/{communityId}")
    public ResponseEntity<List<PostsDTO>> getPostsByCommunity(@PathVariable("communityId") int communityId) {
        System.out.println("[PostController Debug] Getting posts for community ID: " + communityId);

        List<PostsDTO> posts = pd.getPostsByCommunityId(communityId); // PostsDAO에 getPostsByCommunityId 메서드가 필요함

        // 각 게시글에 대해 미디어 정보와 댓글 수, 실제 댓글 목록을 설정
        for (PostsDTO post : posts) {
            post.setMediaList(pd.getPostMedia(post.getPostId()));
            post.setCommentCount(pd.countComments(post.getPostId()));
            post.setComments(pd.getCommentsByPostId(post.getPostId()));
        }
        System.out.println("[PostController Debug] Found " + posts.size() + " posts for community ID: " + communityId);
        return ResponseEntity.ok(posts);
    }

    // 큐레이션 글 관련 입력
    // @PostMapping("/curationInsert")
    // public ResponseEntity<?> postCurationData(@RequestBody PostForCurationDTO postForCurationDTO) {

    //     pd.postForCuration(postForCurationDTO);
        
    //     return ResponseEntity.ok("등록완료료");

    // }
    
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable("postId") String postId) {
        System.out.println("                     게시글 삭제 요청 ID: " + postId);
        int postid = Integer.parseInt(postId);
        try {
            PostsDTO existingPost = pd.getPostById(postid);
            if (existingPost == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("게시글이 존재하지 않습니다.");
            }
            
            pd.deletePost(postid); // DAO에 삭제 처리 위임
            return ResponseEntity.ok("게시글이 삭제되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시글 삭제 실패");
        }
    }

    // @GetMapping("/board/{boardId}")
    // public ResponseEntity<?> getPostsByBoardId(@PathVariable("boardId") int boardId) {
    //     System.out.println("	특정 커뮤니티의 게시글 조회 요청 : boardId = " + boardId);
    //     try {
    //         List<PostsDTO> posts = pd.getPostsByBoardId(boardId);

    //         for (PostsDTO post : posts) {
    //             int postId = post.getPostId();
    //             int count = pd.countComments(postId);
    //             post.setCommentCount(count);
    //             post.setComments(pd.getCommentsByPostId(postId));
    //             post.setMediaList(pd.getPostMedia(postId));
    //         }

    //         return ResponseEntity.ok(posts);
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return ResponseEntity.internalServerError().body("게시글 조회 실패");
    //     }
    // }

    // @GetMapping("/board/{boardId}/{mode}")
    // public ResponseEntity<?> getPostsByBoardIdModeAndQuery(
    //     @PathVariable("boardId") int boardId,
    //     @PathVariable("mode") String mode,
    //     @RequestParam(value = "query", required = false) String query
    // ) {
    //     System.out.println("요청: boardId = " + boardId + ", mode = " + mode + ", query = " + query);
    
    //     try {
    //         List<PostsDTO> posts;
    
    //         if (query != null && !query.trim().isEmpty()) {
    //             // 검색 쿼리가 있을 때
    //             switch (mode.toLowerCase()) {
    //                 case "all":
    //                     posts = pd.searchPostsByBoardIdAndQuery(boardId, query);
    //                     break;
    //                 case "general":
    //                     posts = pd.searchGeneralPostsByBoardIdAndQuery(boardId, query);
    //                     break;
    //                 case "curation":
    //                     posts = pd.searchCurationPostsByBoardIdAndQuery(boardId, query);
    //                     break;
    //                 default:
    //                     return ResponseEntity.badRequest().body("유효하지 않은 mode 값입니다.");
    //             }
    //         } else {
    //             // 검색 쿼리가 없을 때
    //             switch (mode.toLowerCase()) {
    //                 case "all":
    //                     posts = pd.getPostsByBoardId(boardId);
    //                     break;
    //                 case "general":
    //                     posts = pd.getGeneralPostsByBoardId(boardId);
    //                     break;
    //                 case "curation":
    //                     posts = pd.getCurationPostsByBoardId(boardId);
    //                     break;
    //                 default:
    //                     return ResponseEntity.badRequest().body("유효하지 않은 mode 값입니다.");
    //             }
    //         }
    
    //         for (PostsDTO post : posts) {
    //             int postId = post.getPostId();
    //             post.setCommentCount(pd.countComments(postId));
    //             post.setComments(pd.getCommentsByPostId(postId));
    //             post.setMediaList(pd.getPostMedia(postId));
    //         }
    
    //         return ResponseEntity.ok(posts);
    
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return ResponseEntity.internalServerError().body("게시글 조회 실패");
    //     }
    // }
    
    @GetMapping("/board/{boardId}/{mode}")
    public ResponseEntity<?> getPostsByBoardIdModeAndQuery(
        @PathVariable("boardId") int boardId,
        @PathVariable("mode") String mode,
        @RequestParam(value = "query", required = false) String query
    ) {
        System.out.printf("요청: boardId = %d, mode = %s, query = %s%n", boardId, mode, query);

        try {
            List<PostsDTO> posts;

            // mode 정규화
            String modeNormalized = mode.toLowerCase();

            // 1. mode 유효성 체크
            if (!List.of("all", "general", "curation").contains(modeNormalized)) {
                return ResponseEntity.badRequest().body("유효하지 않은 mode 값입니다.");
            }

            // 2. 게시글 조회
            posts = fetchPosts(boardId, modeNormalized, query);

            // 3. 공통 후처리
            for (PostsDTO post : posts) {
                int postId = post.getPostId();
                post.setCommentCount(pd.countComments(postId));
                post.setComments(pd.getCommentsByPostId(postId));
                post.setMediaList(pd.getPostMedia(postId));

                // 추가: 큐레이션이면 MongoDB에서 세그먼트 붙이기
                if ("curation".equals(modeNormalized)) {
                    List<CurationSegmentDoc> segments = mongoRepo.findByPostId(postId);
                    post.setCurationSegments(segments);
                }
            }

            return ResponseEntity.ok(posts);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("게시글 조회 실패");
        }
    }

    private List<PostsDTO> fetchPosts(int boardId, String mode, String query) {
        boolean isSearch = query != null && !query.trim().isEmpty();
    
        if ("curation".equalsIgnoreCase(mode)) {
            if (isSearch) {
                // 1. MongoDB에서 description에 query 포함된 도큐먼트 검색
                List<CurationSegmentDoc> matchedSegments = mongoRepo.findByDescriptionRegex(query);
    
                // 2. 유니크 postId 추출
                Set<Integer> postIds = matchedSegments.stream()
                    .map(CurationSegmentDoc::getPostId)
                    .collect(Collectors.toSet());
    
                // 3. MySQL에서 boardId가 일치하고 postId가 포함된 글만 필터링
                return postIds.isEmpty() 
                    ? List.of() 
                    : pd.getFilteredPostsByBoardIdAndPostIds(boardId, postIds);
            } else {
                // 검색 없을 때는 boardId 기반으로 MySQL에서 조회
                return pd.getCurationPostsByBoardId(boardId);
            }
        }
    
        // 일반글 or 전체글 처리
        return isSearch
            ? switch (mode) {
                case "all" -> pd.searchPostsByBoardIdAndQuery(boardId, query);
                case "general" -> pd.searchGeneralPostsByBoardIdAndQuery(boardId, query);
                default -> throw new IllegalArgumentException("잘못된 mode");
            }
            : switch (mode) {
                case "all" -> pd.getPostsByBoardId(boardId);
                case "general" -> pd.getGeneralPostsByBoardId(boardId);
                default -> throw new IllegalArgumentException("잘못된 mode");
            };
    }
    


    
    
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable("commentId") String commentId,
            @RequestBody CommentDTO updatedComment) {
        System.out.println("                                  댓글 수정 요청 진입 ID: " + commentId);
        int commentid = Integer.parseInt(commentId);
        try {
            updatedComment.setCommentId(commentid);
            updatedComment.setUpdatedAt(LocalDateTime.now());

            pd.updateComment(updatedComment);

            return ResponseEntity.ok("댓글이 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 수정 실패");
        }
    }
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable("commentId") String commentId) {
        System.out.println("                                            댓글 삭제 요청 진입 ID: " + commentId);
        int commentid = Integer.parseInt(commentId);
        try {
            pd.deleteComment(commentid);
            return ResponseEntity.ok("댓글이 삭제되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 삭제 실패");
        }
    }

}