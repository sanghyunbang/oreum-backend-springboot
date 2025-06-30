package com.oreum.goods.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.oreum.goods.dto.GoodsCartDTO;

@Mapper
public interface GoodsCartDAO {
	
	List<GoodsCartDTO> findUserCart(@Param("id") int id);
	
	Integer existsCart(@Param("userId") int int1, @Param("goodsOptionId") int int2);

	void addCart(GoodsCartDTO dto);

	void removeCart(@Param("id") int id);

	void selRemoveCart(List<Integer> cartIds);

	void selDeleteCart(@Param("goodsOptionIds") List<Integer> goodsOptionIds);
	
}
