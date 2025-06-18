package com.oreum.goods.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.oreum.goods.dto.GoodsOrderDTO;
import com.oreum.goods.dto.OrderItemDTO;

@Mapper
public interface GoodsOrderDAO {

	List<GoodsOrderDTO> findDeliveryList(@Param("userid") int userid);

	void addOrder(GoodsOrderDTO odto);

	void addItemOrder(@Param("items") List<OrderItemDTO> items);
}
