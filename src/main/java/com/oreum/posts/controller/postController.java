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

        // DB 저장하기 1 (게시글 공통 테이블)
        pd.insertpost(postDTO);

        // DB 저장하기 2 (큐레이션 여부 따져서 큐레이션 저장)
        if("curation".equals(postDTO.getType())){
            pd.insertCurationDetail(postDTO);
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
 
}
