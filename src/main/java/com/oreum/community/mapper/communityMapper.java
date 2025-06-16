package com.oreum.community.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.oreum.community.dto.communityDTO;

@Mapper
public interface communityMapper {
    communityDTO getCommunity(@Param("title") String title);
    void insertCommunity(communityDTO communityDTO);
    List<communityDTO> getAllCommunities();
    
}
