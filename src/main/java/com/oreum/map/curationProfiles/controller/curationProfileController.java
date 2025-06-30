package com.oreum.map.curationProfiles.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oreum.map.curationProfiles.DAO.profileForMapDao;
import com.oreum.map.curationProfiles.DTO.BaseGeo;
import com.oreum.map.curationProfiles.DTO.UserForMap;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/profilesForMap")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class curationProfileController {
    private final profileForMapDao profilesMapper;


    @GetMapping("/getProfiles")
    public List<UserForMap> getProfiles(@ModelAttribute BaseGeo baseGeo) {
        return profilesMapper.getProfiles(baseGeo);
    }

    
}
