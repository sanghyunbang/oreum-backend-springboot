package com.oreum.goods.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.oreum.goods.dto.goodsDTO;

@Mapper
public interface GoodsOrderDAO {

	boolean existsGoods(@Param("userId") int int1, @Param("goodsOptionId") int int2);

	goodsDTO addGoods(@Param("user_id") int user_id, @Param("goods_option_id") int goods_option_id, @Param("qty") int qty);

}
