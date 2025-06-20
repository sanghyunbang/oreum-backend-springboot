package com.oreum.auth.Controller;

import com.oreum.auth.dto.CustomOAuth2User;
import com.oreum.auth.dto.UserRecordDTO;
import com.oreum.auth.mapper.UserDao;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class UserController {

    private final UserDao userMapper;

    /**
     *  로그인 복원 시 호출되는 기본 유저 정보
     * - 프론트에서 /api/user 호출 시 로그인 여부 판단 가능
     */
    @GetMapping
    public ResponseEntity<?> getUserInfo(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof CustomOAuth2User user) {
            String email = user.getUserName();
            int userId = userMapper.selectUserIdByEmail(email);
            String nickname = userMapper.userNameByuserId(userId);

            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "email", email,
                "nickname", nickname
            ));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
            "isLoggedIn", false,
            "message", "Unauthorized"
        ));
    }

    /**
     *  userId로 nickname 조회 (예: 게시글 작성 시)
     */
    @GetMapping("/nickname")
    public ResponseEntity<String> getNickname(@RequestParam int userId) {
        String nickName = userMapper.userNameByuserId(userId);
        if (nickName == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(nickName);
    }

    /**
     *  현재 인증 객체만 간단히 확인
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.ok(Map.of("isLoggedIn", false));
        }

        CustomOAuth2User user = (CustomOAuth2User) authentication.getPrincipal();

        return ResponseEntity.ok(Map.of(
            "isLoggedIn", true,
            "username", user.getUserName(),
            "role", user.getAuthorities().iterator().next().getAuthority()
        ));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
    	System.out.println("                             로그아웃 진입");
        
    	for (String name : new String[]{"accessToken", "refreshToken"}) {
            Cookie cookie = new Cookie(name, null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            response.addCookie(cookie);
        }
        return ResponseEntity.ok().body("logout completed");
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRecordDTO userDto) {
    	System.out.println("                회원가입 요청 : "+userDto.getName()
    					   +"                  email : " + userDto.getEmail());
        // 이메일 중복 체크
        if (userMapper.findByEmail(userDto.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "이미 존재하는 이메일입니다."));
        }

        // 비밀번호 해싱 (실제 서비스에서는 BCrypt 등으로 해싱 필요)
        // 임시로 원본 비밀번호를 passwordHash 필드에 넣는다고 가정
        // TODO: BCryptPasswordEncoder 같은 걸로 해시 처리 필수
        userDto.setPasswordHash(userDto.getPasswordHash()); // 실제 해싱 로직 넣기

        userDto.setRole("user");
        userDto.setStatus("active");
        userDto.setPoints(0);
        userDto.setCreatedAt(java.time.LocalDateTime.now());
        userDto.setLastLogin(null);

        userMapper.insertUser(userDto);

        return ResponseEntity.ok(Map.of("message", "회원가입 성공"));
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> userlogin(){
    	System.out.println("                       로그인 진입");
    }
    
}
