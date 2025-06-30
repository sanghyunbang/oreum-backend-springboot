package com.oreum.map.curationProfiles.DAO;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.oreum.map.curationProfiles.DTO.BaseGeo;
import com.oreum.map.curationProfiles.DTO.UserForMap;

@Mapper
public interface profileForMapDao {
    
    List<UserForMap> getProfiles(BaseGeo baseGeo);
}
