package com.oreum.goods.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.oreum.goods.dto.goodsOptionDTO;

@Mapper
public interface GoodsOptionDAO {

	List<goodsOptionDTO> findGoodsOptions(@Param("id") int id);

}
