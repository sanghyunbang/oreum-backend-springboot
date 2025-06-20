package com.oreum.auth.Controller;

import com.oreum.auth.dto.CustomOAuth2User;
import com.oreum.auth.dto.AppUserRecordDTO;
import com.oreum.auth.mapper.AppUserDao;
import com.oreum.external.S3.S3Service; // S3Service 임포트 경로 확인

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile; // MultipartFile 임포트

import java.io.IOException; // IOException 임포트
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/app_user")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AppUserController {

    private final AppUserDao appUserMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final S3Service s3Service; // S3Service 주입

    /**
     * 로그인 복원 시 호출되는 기본 유저 정보
     * - 프론트에서 /api/app_user 호출 시 로그인 여부 판단 가능
     */
    @GetMapping
    public ResponseEntity<?> getUserInfo(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof CustomOAuth2User user) {
            String email = user.getUserName();
            AppUserRecordDTO userInfo = appUserMapper.findByEmail(email);

            if (userInfo != null) {
                return ResponseEntity.ok(Map.of(
                    "userId", userInfo.getUserId(),
                    "email", userInfo.getEmail(),
                    "nickname", userInfo.getNickname(),
                    "name", userInfo.getName() != null ? userInfo.getName() : "",
                    "profileImage", userInfo.getProfileImage() != null ? userInfo.getProfileImage() : "",
                    "address", userInfo.getAddress() != null ? userInfo.getAddress() : ""
                ));
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
            "isLoggedIn", false,
            "message", "Unauthorized"
        ));
    }

    /**
     * userId로 nickname 조회 (예: 게시글 작성 시)
     */
    @GetMapping("/nickname")
    public ResponseEntity<String> getNickname(@RequestParam int userId) {
        String nickName = appUserMapper.userNameByuserId(userId);
        if (nickName == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(nickName);
    }

    /**
     * 현재 인증 객체만 간단히 확인
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.ok(Map.of("isLoggedIn", false));
        }

        // OAuth2 로그인 사용자를 위한 로직
        if (authentication.getPrincipal() instanceof CustomOAuth2User user) {
             return ResponseEntity.ok(Map.of(
                "isLoggedIn", true,
                "username", user.getUserName(),
                "role", user.getAuthorities().iterator().next().getAuthority()
            ));
        }
        // 일반 로그인 사용자를 위한 로직 (UserDetailsService에서 반환하는 User 타입)
        else if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User user) {
            return ResponseEntity.ok(Map.of(
                "isLoggedIn", true,
                "username", user.getUsername(),
                "role", user.getAuthorities().iterator().next().getAuthority()
            ));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("isLoggedIn", false, "message", "인증된 사용자가 아닙니다."));
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
    public ResponseEntity<?> registerUser(@RequestBody AppUserRecordDTO userDto) {
    	System.out.println("                회원가입 요청 : "+userDto.getName()
    					   +"                  email : " + userDto.getEmail());
        if (appUserMapper.findByEmail(userDto.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "이미 존재하는 이메일입니다."));
        }

        userDto.setPasswordHash(bCryptPasswordEncoder.encode(userDto.getPasswordHash()));

        userDto.setRole("user");
        userDto.setStatus("active");
        userDto.setPoints(0);
        userDto.setCreatedAt(LocalDateTime.now());
        userDto.setLastLogin(null);

        appUserMapper.insertUser(userDto);

        return ResponseEntity.ok(Map.of("message", "회원가입 성공"));
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody AppUserRecordDTO userDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        }
        
        String currentEmail;
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomOAuth2User) {
            currentEmail = ((CustomOAuth2User) principal).getUserName();
        } else if (principal instanceof org.springframework.security.core.userdetails.User) {
            currentEmail = ((org.springframework.security.core.userdetails.User) principal).getUsername();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "인증된 사용자 정보를 가져올 수 없습니다."));
        }

        if (!currentEmail.equals(userDto.getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "권한이 없습니다."));
        }

        AppUserRecordDTO existingUser = appUserMapper.findByEmail(currentEmail);
        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "사용자를 찾을 수 없습니다."));
        }

        if (userDto.getPasswordHash() != null && !userDto.getPasswordHash().isEmpty()) {
            existingUser.setPasswordHash(bCryptPasswordEncoder.encode(userDto.getPasswordHash()));
        }

        existingUser.setName(userDto.getName());
        existingUser.setNickname(userDto.getNickname());
        existingUser.setAddress(userDto.getAddress());
        // 프로필 이미지 URL은 별도의 업로드 API를 통해 업데이트되므로, 여기서는 기존 값 유지
        // existingUser.setProfileImage(userDto.getProfileImage()); 

        appUserMapper.updateUser(existingUser);

        return ResponseEntity.ok(Map.of("message", "사용자 정보가 성공적으로 업데이트되었습니다."));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        }
        
        String currentEmail;
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomOAuth2User) {
            currentEmail = ((CustomOAuth2User) principal).getUserName();
        } else if (principal instanceof org.springframework.security.core.userdetails.User) {
            currentEmail = ((org.springframework.security.core.userdetails.User) principal).getUsername();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "인증된 사용자 정보를 가져올 수 없습니다."));
        }

        AppUserRecordDTO existingUser = appUserMapper.findByEmail(currentEmail);
        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "사용자를 찾을 수 없습니다."));
        }

        appUserMapper.deleteUser(existingUser.getUserId());

        for (String name : new String[]{"accessToken", "refreshToken"}) {
            Cookie cookie = new Cookie(name, null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            response.addCookie(cookie);
        }

        return ResponseEntity.ok(Map.of("message", "회원 탈퇴가 완료되었습니다."));
    }

    // 새로운 프로필 이미지 업로드 엔드포인트
    @PostMapping("/profile-image")
    public ResponseEntity<?> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        }

        String email;
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomOAuth2User) {
            email = ((CustomOAuth2User) principal).getUserName();
        } else if (principal instanceof org.springframework.security.core.userdetails.User) {
            email = ((org.springframework.security.core.userdetails.User) principal).getUsername();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "인증된 사용자 정보를 가져올 수 없습니다."));
        }

        try {
            // S3Service.uploadFile 호출 방식: directory 인자 없이 MultipartFile만 전달
            String imageUrl = s3Service.uploadFile(file); // ★★★ 이 부분이 변경된 핵심 ★★★

            AppUserRecordDTO user = appUserMapper.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "사용자를 찾을 수 없습니다."));
            }
            
            // 기존 프로필 이미지 삭제 로직 (S3Service에 deleteFile 메서드 구현 시 주석 해제)
            // if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            //     try {
            //         s3Service.deleteFile(user.getProfileImage());
            //         System.out.println("기존 프로필 이미지 삭제 성공: " + user.getProfileImage());
            //     } catch (Exception e) {
            //         System.err.println("기존 프로필 이미지 삭제 실패: " + e.getMessage());
            //     }
            // }

            user.setProfileImage(imageUrl);
            appUserMapper.updateUser(user);

            return ResponseEntity.ok(Map.of("message", "프로필 이미지가 성공적으로 업데이트되었습니다.", "profileImageUrl", imageUrl));

        } catch (IOException e) { // IOException 캐치 블록: S3Service의 uploadFile이 IOException을 던질 경우에 대비
            System.err.println("파일 업로드 중 I/O 오류 발생: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "파일 업로드 중 I/O 오류가 발생했습니다.", "error", e.getMessage()));
        } catch (Exception e) { // 그 외 모든 예외를 캐치
            System.err.println("프로필 이미지 처리 중 예상치 못한 오류: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "프로필 이미지 처리 중 오류가 발생했습니다.", "error", e.getMessage()));
        }
    }
}