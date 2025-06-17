package com.oreum.posts.controller;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.oreum.external.S3.S3Service;
import com.oreum.posts.dao.PostsDAO;
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
                int count = pd.countComments(post.getPostId());
                post.setCommentCount(count); // 댓글 수 채워 넣기
            }

            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("게시글 목록 불러오기 실패");
        }
    }
 
}
