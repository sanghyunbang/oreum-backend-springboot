package com.oreum.posts.controller;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.oreum.external.S3.S3Service;
import com.oreum.posts.dao.PostsDAO;
import com.oreum.posts.dto.PostsDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true") //  CORS 허용
public class postController {
	@Autowired PostsDAO pd;

    private final S3Service s3Service;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<List<String>> uploadPost(
        @RequestParam("title") String title,
        @RequestParam("content") String content,
        @RequestParam("category") String category, 
        @RequestParam("mediaFiles") List<MultipartFile> mediaFiles
    ) {
        List<String> uploadedUrls = new ArrayList<>();

        for (MultipartFile file : mediaFiles) {
            String uploadedUrl = s3Service.uploadFile(file);
            uploadedUrls.add(uploadedUrl);
        }

        // TODO: title, content, category, uploadedUrls 를 DB에 저장하는 로직도 나중에 추가 가능

        return ResponseEntity.ok(uploadedUrls);
    }
    
    //게시글 등록
    @PostMapping("/insert")
    public ResponseEntity<String> insertposts( 
    		@RequestParam("title") String title,
    	    @RequestParam("content") String content,
    	    @RequestParam("type") String type,
    	    @RequestParam("userId") int userId) {
    	
    	System.out.println("포스트 진입");
    	
    	PostsDTO post = new PostsDTO();
        post.setTitle(title);
        post.setContent(content);
        post.setType(type);
        post.setUserId(userId);
    	
        pd.insertpost(post);
        
        return ResponseEntity.ok("게시글 등록 완료");
    }
}
