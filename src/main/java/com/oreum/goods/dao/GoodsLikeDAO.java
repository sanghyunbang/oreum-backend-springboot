package com.oreum.goods.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.oreum.goods.dto.GoodsLikedDTO;

@Mapper
public interface GoodsLikeDAO {
	int insertLike(@Param("userId") int userId, @Param("goodsId") int goodsId);
    int deleteLike(@Param("userId") int userId, @Param("goodsId") int goodsId);
    boolean existsLike(@Param("userId") int userId, @Param("goodsId") int goodsId);
	List<GoodsLikedDTO> listLiked(@Param("userId") int userId);
}
