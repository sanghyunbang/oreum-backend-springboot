package com.oreum.auth.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.oreum.auth.dto.UserRecordDTO;

@Mapper
public interface UserDao {

    UserRecordDTO findByEmail(@Param("email") String email);
    void insertUser(UserRecordDTO user);
    void updateLoginInfo(UserRecordDTO user);
    Integer selectUserIdByEmail(@Param("email") String email);
    String userNameByuserId(@Param("userId") int userId);
    UserRecordDTO findByUserId(@Param("userId") int userId);
    void updateUserDetails(UserRecordDTO user);
    void deleteUserById(@Param("userId") int userId);


}
