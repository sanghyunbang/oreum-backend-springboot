package com.oreum.community.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.oreum.community.dto.FeednameDTO;
import com.oreum.community.dto.MyCommunityJoinRequest;
import com.oreum.community.dto.communityDTO;
import com.oreum.posts.dto.MyFeedDTO;
import com.oreum.posts.dto.PostsDTO;

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
    int getFeedIdByuserId(@Param("feedname") String feedname);
    List<MyFeedDTO> getBoardIdsByuserId(@Param("userId") int userId ,@Param("feedId") int feedId);
    int getfeedIdByuserId2(@Param("feedname") String feedname);
    List<PostsDTO> getBoardIdByFindposts(@Param("boardId") int BoardId);
    
    //테스트용
    List<PostsDTO> testcall(@Param("boardId") int BoarId);

    // 관심 커뮤니티
    // 커뮤니티 이름으로 board_id 조회
    Integer getBoardIdByTitle(@Param("title") String title);

    // 로그등록 (관심커뮤니티)
    int insertMyCommunity(@Param("boardId") int boardId, @Param("userId") int userId);

    // my_Community에서 불러오기
    List<String> getTitlesByUserId(@Param("userId") int userId);

    // 서브 커뮤니티 탈퇴
    void leaveCommunity(@Param("userId") int userId, @Param("communityTitle") String communityTitle);


}
