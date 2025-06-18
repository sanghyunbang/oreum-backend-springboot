package com.oreum.posts.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import com.oreum.posts.dto.CommentDTO;
import com.oreum.posts.dto.PostLikeDTO;
import com.oreum.posts.dto.PostsDTO;

@Mapper
public interface PostsDAO {
		
    void insertpost(PostsDTO postDTO);

	void insertCurationDetail(PostsDTO postDTO);
	void insertMeetingDetail(PostsDTO postDTO);

	void insertPostMedia(@Param("postId") int postID,
						 @Param("mediaType") String mediaType,
						 @Param("mediaUrl") String mediaUrl);
	List<PostsDTO> getAllPosts();
	int countComments(@Param("postId") int postId);
	
	public PostsDTO getPostById(int postId);
	List<CommentDTO> getCommentsByPostId(int postId);
	public void insertComment(CommentDTO comment);
	
	void insertPostLike(PostLikeDTO likeDTO);
	void deletePostLike(PostLikeDTO likeDTO);



}
