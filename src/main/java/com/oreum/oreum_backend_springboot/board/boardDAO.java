package com.oreum.oreum_backend_springboot.board;

import java.util.ArrayList;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface boardDAO {
	ArrayList<boardDTO> getlist();

}
