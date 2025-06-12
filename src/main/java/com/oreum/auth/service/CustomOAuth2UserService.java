package com.oreum.auth.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.oreum.auth.dto.CustomOAuth2User;
import com.oreum.auth.dto.GoogleResponse;
import com.oreum.auth.dto.NaverResponse;
import com.oreum.auth.dto.OAuth2Response;
import com.oreum.auth.dto.UserDTO;
import com.oreum.auth.dto.UserRecordDTO;
import com.oreum.auth.mapper.UserDao;
import com.oreum.auth.users.util.NickNameUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserDao userMapper;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    
        OAuth2User oAuth2User = super.loadUser(userRequest);
    
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response;
    
        if (registrationId.equals("naver")) {
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals("google")) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else {
            throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        }
    
        // OAuth에서 받은 이메일
        String email = oAuth2Response.getEmail();
        UserRecordDTO user = userMapper.findByEmail(email);
    
        if (user == null) {

            // 신규 사용자 저장
            UserRecordDTO newUser = new UserRecordDTO();
            newUser.setEmail(email);
    
            newUser.setName(oAuth2Response.getName());// 실명 저장
            newUser.setNickname(NickNameUtil.generateRandomNickname());      // 랜덤하게 Unique하게 생성
            
            newUser.setRole("user");
            newUser.setStatus("active");
            newUser.setPoints(0);
            newUser.setCreatedAt(LocalDateTime.now());
            newUser.setLastLogin(LocalDateTime.now());
    
            userMapper.insertUser(newUser);
            user = newUser;
        } else {
            // 기존 사용자 로그인 정보 갱신
            user.setLastLogin(LocalDateTime.now());
            userMapper.updateLoginInfo(user);
        }
    
        // 인증용 DTO로 변환
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(user.getEmail());     // 인증용 고유 ID (username 역할)
        userDTO.setName(user.getName());          // 실명 사용
        userDTO.setRole(user.getRole());          // user / admin 등
    
        return new CustomOAuth2User(userDTO);
    }    
}
