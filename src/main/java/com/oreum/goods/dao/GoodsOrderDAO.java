package com.oreum.goods.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import com.oreum.goods.dto.GoodsOrderDTO;
import com.oreum.goods.dto.OrderItemDTO;

@Mapper
public interface GoodsOrderDAO {

	List<GoodsOrderDTO> findDeliveryList(@Param("userId") int userid);

	void addOrder(GoodsOrderDTO odto);

	void addItemOrder(@Param("items") List<OrderItemDTO> items);

	@Update("UPDATE orders SET status = '배송중', status_updated_at = NOW() WHERE status = '결제완료' AND status_updated_at <= #{threshold}")
	void updateToShipping(LocalDateTime minusMinutes);

	@Update("UPDATE orders SET status = '배송완료', status_updated_at = NOW() WHERE status = '배송중' AND status_updated_at <= #{threshold}")
	void updateToDelivered(LocalDateTime minusMinutes);
}
