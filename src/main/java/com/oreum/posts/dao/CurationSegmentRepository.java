package com.oreum.posts.dao;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.oreum.posts.dto.CurationSegmentDoc;
import java.util.List;

@Repository
public interface CurationSegmentRepository extends MongoRepository<CurationSegmentDoc, String>{
    List<CurationSegmentDoc> findByPostId(Integer postId);

    @Query("{ '$or': [ " +
    "{ 'pointerName': { $regex: ?0, $options: 'i' } }, " +
    "{ 'description': { $regex: ?0, $options: 'i' } } " +
    "] }")
    List<CurationSegmentDoc> searchByKeyword(String keyword);

    @Query("{ 'boardId': ?0, 'description': { $regex: ?1, $options: 'i' } }")
    List<CurationSegmentDoc> findByBoardIdAndDescriptionRegex(int boardId, String query);

}
