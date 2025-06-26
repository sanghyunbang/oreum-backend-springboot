package com.oreum.posts.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.oreum.posts.dto.CurationSegmentDoc;
import java.util.List;


public interface CurationSegmentRepository extends MongoRepository<CurationSegmentDoc, String>{
    List<CurationSegmentDoc> findByPostId(Integer postId);
}
