package com.oreum.search.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.oreum.community.dto.communityDTO;
import com.oreum.community.mapper.communityMapper;
import com.oreum.posts.dto.PostsDTO;
import com.oreum.search.DAO.MySqlPostMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MySqlPostService {
    
    private final MySqlPostMapper postMapper;

    private final communityMapper commMapper;

    public List<PostsDTO> search(String query, String communityName) {
        try {
            communityDTO communityDto = commMapper.getCommunity(communityName);
            int boardId = communityDto.getBoardId();
    
            return postMapper.searchPosts(query, boardId);
        } catch (Exception e) {
            e.printStackTrace(); // 에러 원인을 콘솔 출력
            throw new RuntimeException("검색 중 오류 발생", e);
        }
    }
    
}
