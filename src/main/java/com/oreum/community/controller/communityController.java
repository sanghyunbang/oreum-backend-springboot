package com.oreum.community.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oreum.community.dto.communityDTO;
import com.oreum.community.mapper.communityMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true") //  CORS 허용

public class communityController {

    private final communityMapper _comm;
    @GetMapping("/{communityName}")
    public ResponseEntity<communityDTO> getCommunityName(@PathVariable("communityName") String communityName) {
        System.out.println("들어오긴함");
        communityDTO dto = _comm.getCommunity(communityName);
        return ResponseEntity.ok(dto);
    }
    
    
    
}
