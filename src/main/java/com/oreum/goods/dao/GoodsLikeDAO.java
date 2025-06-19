package com.oreum.goods.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GoodsLikeDAO {
	int insertLike(@Param("userId") int userId, @Param("goodsId") int goodsId);
    int deleteLike(@Param("userId") int userId, @Param("goodsId") int goodsId);
    boolean existsLike(@Param("userId") int userId, @Param("goodsId") int goodsId);
}
