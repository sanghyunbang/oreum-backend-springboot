package com.oreum.posts.dto;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
public class CurationSegmentDoc {
    private String segmentKey;
    private int order;
    private String pointerName;
    private String difficulty;
    private String caution;
    private List<String> facility; 
    private Object geometry;// GeoJSON 형태
    private List<String> media;// S3에 저장된 URL
    private Integer postId;
}
