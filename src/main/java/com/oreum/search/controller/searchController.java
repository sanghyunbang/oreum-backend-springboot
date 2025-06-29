package com.oreum.search.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oreum.posts.dto.PostsDTO;
import com.oreum.search.service.MySqlPostService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true") // CORS 허용
public class searchController {

    private final MySqlPostService mySqlPostService;

    // 특정 서브 커뮤니티 내에서

    @GetMapping("/feedSearch/{communityName}")
    public ResponseEntity<List<PostsDTO>> searchAllPostsForFeed(@RequestParam("query") String query, @PathVariable("communityName") String communityName) {

        try {
            List<PostsDTO> mySqlPosts = mySqlPostService.search(query, communityName);
            System.out.println("쿼리 찍어보기"+query);
            return ResponseEntity.ok(mySqlPosts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
    }

    // 전체에서
    
}
