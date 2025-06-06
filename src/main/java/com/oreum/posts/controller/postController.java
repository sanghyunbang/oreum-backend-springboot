package com.oreum.posts.controller;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.oreum.external.S3.S3Service;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true") //  CORS 허용
public class postController {

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
}
