package com.oreum.goods.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
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

	void cancelOrder(@Param("id") int id);
	
	@Delete("DELETE FROM orders WHERE order_id=#{id}")
	void deleteOrder(@Param("id") int id);

	void updateReview(@Param("orderItemId") int order_item_id);

	@Update("UPDATE users SET points = points+#{points} where user_id=#{id}")
	void addPoints(@Param("id") int userId, @Param("points") int points);

	@Select("SELECT points FROM users where user_id=#{userId}")
	String getUserPoints(@Param("userId") int userId);

	void updatePoints(GoodsOrderDTO odto);
	
}
