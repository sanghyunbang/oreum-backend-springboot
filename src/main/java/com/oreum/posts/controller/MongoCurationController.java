package com.oreum.posts.controller;

import java.util.stream.Collectors;
import java.util.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oreum.external.S3.S3Service;
import com.oreum.posts.dao.CurationSegmentRepository;
import com.oreum.posts.dto.CurationSegmentDoc;
import com.oreum.posts.service.CurationSegmentService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequiredArgsConstructor
@RequestMapping("/mongo")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")

public class MongoCurationController {

    private final CurationSegmentRepository segmentRepository;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper;

    // 몽고 검색 관련 서비스
    private final CurationSegmentService service;
    
    @PostMapping(value = "/curationSegments", consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadsCurationSegments(
        @RequestPart("postId") String postIdstr,
        @RequestPart("segments") String segmentsJson,
        @RequestParam Map<String, MultipartFile> fileMap
    ) {
        try{
            System.out.println("[1] postId: " + postIdstr);
            System.out.println("[2] segmentsJson: " + segmentsJson);
            System.out.println("[3] fileMap keys: " + fileMap.keySet());

            // JSON으로 날아온 segments 파싱하기
            List<CurationSegmentDoc> segments = Arrays.asList(
                objectMapper.readValue(segmentsJson, CurationSegmentDoc[].class)
            );

            // 각 segmen에 맞게 media url채워넣기
            for (CurationSegmentDoc segment: segments) {
                System.out.println("[4] 파싱된 segment 개수: " + segments.size());

                List<String> media = new ArrayList<>();

                for (int i = 0; ; i++) {
                    String mediaKey = "media-" + segment.getSegmentKey() + "-" + i;
                    MultipartFile file = fileMap.get(mediaKey);
                    if (file == null) break;

                    System.out.println("[6] 업로드할 파일: " + mediaKey + ", 이름: " + file.getOriginalFilename());
                    String url = s3Service.uploadFile(file);
                    media.add(url);
                }

                // segment.setPostId(Integer.parseInt(curationId));
                segment.setPostId(Integer.parseInt(postIdstr));
                segment.setMedia(media);
            }
            
            // MongoDB 저장
            System.out.println("[7] MongoDB 저장 시도 중...");
            segmentRepository.saveAll(segments);
            System.out.println("[8] MongoDB 저장 성공");
            return ResponseEntity.ok("Mongo 저장 성공");

        } catch (Exception e) {
            System.out.println("[9] 에러 발생: " + e.toString());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Mongo 저장 실패: "+ e.getMessage());
        }
    }

    @GetMapping("/curationSegments/{postId}")
    public ResponseEntity<?> getSegmentsByPostId(@PathVariable("postId") int postId) {
        try {
            List<CurationSegmentDoc> segments = segmentRepository.findByPostId(postId);
            return ResponseEntity.ok(segments);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("조회 실패 "+ e.getMessage());
        }    
    }

    @GetMapping("/search")
    public ResponseEntity<List<CurationSegmentDoc>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(service.searchByKeyword(keyword));
    }
    
       
}
