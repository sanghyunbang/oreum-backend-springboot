package com.oreum.auth.Controller;

import com.oreum.auth.dto.CustomOAuth2User;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

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
}