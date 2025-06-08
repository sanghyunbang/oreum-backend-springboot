package com.oreum.auth.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.oreum.auth.dto.UserRecordDTO;

@Mapper
public interface UserDao {

    UserRecordDTO findByEmail(@Param("email") String email);
    void insertUser(UserRecordDTO user);
    void updateLoginInfo(UserRecordDTO user);
}
