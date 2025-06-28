package com.oreum.community.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.oreum.community.dto.communityDTO;
import com.oreum.posts.dto.MyFeedDTO;

@Mapper
public interface communityMapper {
    communityDTO getCommunity(@Param("title") String title);
    void insertCommunity(communityDTO communityDTO);
    List<communityDTO> getAllCommunities();
    void insertFeed(@Param("userId") int userId, @Param("feedname") String feedname);
    
    int getFeedNameById(@Param("feedname") String feedname);
    void insertFeedBoard(@Param("feedId") int feedId,
    					 @Param("userId") int userId, 
    					 @Param("boardId") int boardId 
    					 );
    List<MyFeedDTO> getFeedsByUserId(@Param("userId") int userId);
}
