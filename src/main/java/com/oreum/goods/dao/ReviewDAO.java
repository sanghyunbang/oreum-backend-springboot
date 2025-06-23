package com.oreum.goods.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.oreum.goods.dto.ReviewDTO;

@Mapper
public interface ReviewDAO {
	void insertReview(ReviewDTO dto);

	List<ReviewDTO> selectReview(@Param("id") int id);
}
