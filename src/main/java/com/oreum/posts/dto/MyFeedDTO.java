package com.oreum.posts.dto;

import java.util.List;

import lombok.Data;

@Data
public class MyFeedDTO {
	private int id;
	private int feedId;
    private int userId;
    private int boardId;
    private String feedname;
    private List<Integer> boardIdList;

}
