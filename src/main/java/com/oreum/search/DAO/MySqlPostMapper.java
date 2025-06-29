package com.oreum.search.DAO;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.oreum.posts.dto.PostsDTO;

@Mapper
public interface MySqlPostMapper {

    List<PostsDTO> searchPosts(@Param("query") String query, @Param("boardId") int boardId);
    
}
