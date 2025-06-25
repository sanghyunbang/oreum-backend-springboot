package com.oreum.posts.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.oreum.posts.dto.CurationSegmentDoc;

public interface CurationSegmentRepository extends MongoRepository<CurationSegmentDoc, String>{
    
}
