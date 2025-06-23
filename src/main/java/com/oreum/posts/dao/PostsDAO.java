package com.oreum.posts.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import com.oreum.posts.dto.BookmarkDTO;
import com.oreum.posts.dto.CommentDTO;
import com.oreum.posts.dto.MediaDTO;
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

	List<MediaDTO> getPostMedia(@Param("postId") int postId);					 
	List<PostsDTO> getAllPosts();
	int countComments(@Param("postId") int postId);
	
	public PostsDTO getPostById(int postId);
	List<CommentDTO> getCommentsByPostId(int postId);
	public void insertComment(CommentDTO comment);
	
	// 좋아요 여부 확인
	public PostLikeDTO getPostLike(@Param("postId") int postId, @Param("userId") int userId);
	// 좋아요 추가
	void insertPostLike(PostLikeDTO likeDTO);
	// 좋아요 삭제
	void deletePostLike(@Param("postId") int postId, @Param("userId") int userId);
	// 게시글 좋아요 수 증가
	void incrementPostLikeCount(int postId);
	// 게시글 좋아요 수 감소
	void decrementPostLikeCount(int postId);
	
	// 북마크 여부 확인
	BookmarkDTO getBookmark(@Param("postId") int postId, @Param("userId") int userId);
	// 북마크 추가
	void insertBookmark(BookmarkDTO bookmark);
	// 북마크 삭제
	void deleteBookmark(@Param("postId") int postId, @Param("userId") int userId);
	// 특정 유저가 북마크한 postId 목록
	List<Integer> getBookmarkedPostIdsByUser(int userId);

	// 내가 쓴 글
	List<PostsDTO> getPostsByUserId(@Param("userId") int userId);

	// 내가 쓴 덧글
	List<CommentDTO> getCommentsByUserId(@Param("userId") int userId);

	// 좋아요한 게시물 ID 리스트
	List<Integer> getLikedPostIdsByUser(@Param("userId") int userId);


}
