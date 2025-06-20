package com.oreum.auth.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.oreum.auth.dto.AppUserRecordDTO;

// 사용자 데이터베이스 접근을 위한 DAO (Data Access Object) 인터페이스
@Mapper
public interface AppUserDao {
    // 이메일로 사용자 정보 조회
    AppUserRecordDTO findByEmail(@Param("email") String email);
    // 새로운 사용자 등록
    void insertUser(AppUserRecordDTO user);
    // 로그인 시 마지막 로그인 정보 업데이트
    void updateLoginInfo(AppUserRecordDTO user);
    // 이메일로 사용자 ID 조회
    Integer selectUserIdByEmail(@Param("email") String email);
    // 사용자 ID로 닉네임 조회
    String userNameByuserId(@Param("userId") int userId);
    // 사용자 정보 업데이트
    void updateUser(AppUserRecordDTO user);
    // 사용자 삭제 (회원 탈퇴)
    void deleteUser(@Param("userId") int userId);
    // 사용자 ID로 프로필 이미지 URL 조회
    String selectProfileImageByUserId(@Param("userId") int userId);
}
