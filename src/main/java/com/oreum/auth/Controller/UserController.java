package com.oreum.auth.Controller;

import com.oreum.auth.dto.CustomOAuth2User;
import com.oreum.auth.mapper.UserDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
	@Autowired UserDao ud;

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
    
    @GetMapping("/idget")
    public ResponseEntity<Integer> getid(@RequestParam String email) {
    	Integer userId = ud.selectUserIdByEmail(email);
    	System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡemail로 id조회 진입ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
    	if (userId == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userId);
    }
}