package com.oreum.community.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.oreum.community.dto.communityDTO;

@Mapper
public interface communityMapper {
    communityDTO getCommunity(@Param("name") String name);
}
