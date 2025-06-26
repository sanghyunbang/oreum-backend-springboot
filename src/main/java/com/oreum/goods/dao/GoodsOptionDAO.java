package com.oreum.goods.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.oreum.goods.dto.OrderItemDTO;
import com.oreum.goods.dto.goodsOptionDTO;

@Mapper
public interface GoodsOptionDAO {

	List<goodsOptionDTO> findGoodsOptions(@Param("id") int id);

	void updateQtyOne(OrderItemDTO item);

	void insertGoodsOptions(@Param("options") List<goodsOptionDTO> options);

	List<goodsOptionDTO> findAllItemGoods();

}
