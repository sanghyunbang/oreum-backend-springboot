package com.oreum.posts.controller;

import java.util.stream.Collectors;
import java.util.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oreum.external.S3.S3Service;
import com.oreum.posts.dao.CurationSegmentRepository;
import com.oreum.posts.dto.CurationSegmentDoc;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/mongo")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")

public class MongoCurationController {

    private final CurationSegmentRepository segmentRepository;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper;
    
    @PostMapping(value = "/curationSegments", consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadsCurationSegments(
        @RequestPart("curationId") String curationId,
        @RequestPart("segments") String segmentsJson,
        @RequestPart(required = false) List<MultipartFile> mediaFiles
    ) {
        try{

            // JSON으로 날아온 segments 파싱하기
            List<CurationSegmentDoc> segments = Arrays.asList(
                objectMapper.readValue(segmentsJson, CurationSegmentDoc[].class)
            );

            // mediaFiles -> 키에 맞게 맵으로 변환 (media-1-2-0 이런 식)
            Map<String, MultipartFile> mediaMap = Optional.ofNullable(mediaFiles)
                    .orElse(List.of())
                    .stream()
                    .collect(Collectors.toMap(
                        MultipartFile::getName, 
                        file->file
                    ));

            // 각 segmen에 맞게 media url채워넣기
            for (CurationSegmentDoc segment: segments) {
                List<String> mediaUrls = new ArrayList<>();
                for (int i = 0; ; i++) {
                    String mediaKey = "media-" + segment.getSegmentKey() + "-" + i;
                    MultipartFile file = mediaMap.get(mediaKey);
                    if (file == null) break;

                    String url = s3Service.uploadFile(file);
                    mediaUrls.add(url);
                }

                segment.setPostId(Integer.parseInt(curationId));
                segment.setMediaUrls(mediaUrls);
            }
            
            // MongoDB 저장
            segmentRepository.saveAll(segments);
            return ResponseEntity.ok("Mongo 저장 성공");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Mongo 저장 실패: "+ e.getMessage());
        }
    }
    
}
