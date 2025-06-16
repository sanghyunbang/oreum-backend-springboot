package com.oreum.goods.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.oreum.goods.dto.goodsDTO;

@Mapper
public interface goodsDAO {

	List<goodsDTO> findAllGoods();

	List<goodsDTO> findGoods(@Param("id")int id);

	List<goodsDTO> findGoodsOptions(@Param("id") int id);
}
