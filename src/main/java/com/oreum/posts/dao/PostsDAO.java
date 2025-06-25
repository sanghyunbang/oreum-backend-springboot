package com.oreum.posts.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import com.oreum.posts.dto.BookmarkDTO;
import com.oreum.posts.dto.CommentDTO;
import com.oreum.posts.dto.MediaDTO;
import com.oreum.posts.dto.PostForCurationDTO;
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
	// 북마크한 게시물 ID 리스트
	List<Integer> getbookmarkIdsByUser(@Param("userId") int userId);
	//커뮤니티에서 받은 boardId로 포스트 호출
	List<PostsDTO> getPostsByBoardId(int boardId);
	//게시글 수정
	void updatePost(PostsDTO postDTO);
	//게시물 삭제
	void deletePost(@Param("postId") int postId);
	// 유저 ID로 닉네임 가져오기
	String getNicknameByUserId(@Param("userId") int userId);
	
	// 댓글 수정
	public void updateComment(CommentDTO comment);
	// 댓글 삭제
	public void deleteComment(int commentId);
	
	
	// 커뮤니티 조회 앱
	List<PostsDTO> getPostsByCommunityId(@Param("communityId") int communityId);

	// 큐레이션 글 insert
	void postForCuration(PostForCurationDTO postForCurationDTO);

	// 큐레이션 글 insert 후에 해당 post 값 보내주기기


}
