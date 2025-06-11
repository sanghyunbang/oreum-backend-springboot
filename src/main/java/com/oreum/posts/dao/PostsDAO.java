package com.oreum.posts.dao;

import org.apache.ibatis.annotations.Mapper;

import com.oreum.posts.dto.PostsDTO;

@Mapper
public interface PostsDAO {
	void insertpost(PostsDTO Pd);

}
