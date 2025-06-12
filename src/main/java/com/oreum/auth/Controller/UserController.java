package com.oreum.auth.Controller;

import com.oreum.auth.dto.CustomOAuth2User;
import com.oreum.auth.mapper.UserDao;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true") //  CORS 허용
public class UserController {
	@Autowired UserDao userMapper;

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Map.of("isLoggedIn", false);
        }

        CustomOAuth2User user = (CustomOAuth2User) authentication.getPrincipal();

        return Map.of(
            "isLoggedIn", true,
            "username", user.getUserName(),
            "role", user.getAuthorities().iterator().next().getAuthority()
        );
    }
    
    @GetMapping("/nickname")
    public ResponseEntity<String> getNickname(@RequestParam int userId) {
    	System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡuserId로 진입ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");

        String nickName = userMapper.userNameByuserId(userId);
    	if (nickName == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(nickName);
    }

    @GetMapping("/infoForPost")
    public ResponseEntity<?> getUserInfo(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomOAuth2User user) {
            String email = user.getUserName();
            int userId = userMapper.selectUserIdByEmail(email);
            String nickname = userMapper.userNameByuserId(userId);
            return ResponseEntity.ok(Map.of("userId", userId, "nickname", nickname));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 실패");
    }

}