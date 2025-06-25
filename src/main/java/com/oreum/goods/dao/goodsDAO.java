package com.oreum.goods.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.oreum.goods.dto.goodsDTO;

@Mapper
public interface goodsDAO {

	List<goodsDTO> findAllGoods();

	List<goodsDTO> findGoods(@Param("id")int id);

	void decreaseLikes(@Param("userId") int userId, @Param("goodsId") int goodsId);

	void increaseLikes(@Param("userId") int userId, @Param("goodsId") int goodsId);

	void insertGoods(goodsDTO dto);
}
