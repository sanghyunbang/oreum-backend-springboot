package com.oreum.auth.Controller;

import com.oreum.auth.dto.CustomOAuth2User;
import com.oreum.auth.dto.UserRecordDTO;
import com.oreum.auth.jwt.JWTUtil;
import com.oreum.auth.mapper.UserDao;
import com.oreum.external.S3.S3Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class UserController {

    private final UserDao userMapper;
    private final JWTUtil jwtUtil;
    private final S3Service s3;
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
    
    @DeleteMapping
    public ResponseEntity<?> deleteUser(HttpServletRequest request, HttpServletResponse response) {
    	System.out.println("                                      유저탈퇴 진입 id : ");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof CustomOAuth2User user) {
            String email = user.getUserName();
            Integer userId = userMapper.selectUserIdByEmail(email);

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "사용자를 찾을 수 없습니다."));
            }

            // 1. DB에서 사용자 삭제
            userMapper.deleteUserById(userId);

            // 2. 쿠키 제거 (accessToken, refreshToken)
            for (String name : new String[]{"accessToken", "refreshToken"}) {
                Cookie cookie = new Cookie(name, null);
                cookie.setMaxAge(0);
                cookie.setPath("/");
                cookie.setHttpOnly(true);
                cookie.setSecure(false);
                response.addCookie(cookie);
            }

            // 3. SecurityContext 비우기 (선택)
            SecurityContextHolder.clearContext();

            return ResponseEntity.ok(Map.of("message", "회원 탈퇴 완료"));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
    }

    @PostMapping("/details")
    public ResponseEntity<?> getUserDetails(@RequestBody Map<String, Integer> requestBody) {
    	System.out.println("                            사용자 정보 마이페이지");

    	Integer userId = requestBody.get("userId");
        System.out.println("                            POST 요청으로 받은 userId: " + userId);

        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "userId가 필요합니다."));
        }

        UserRecordDTO userDetails = userMapper.findByUserId(userId);
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "사용자 정보를 찾을 수 없습니다."));
        }
        System.out.println("                  프로필 url : " + userDetails.getProfileImage());
        
        return ResponseEntity.ok(userDetails);
    }
    
    @PutMapping("/details")
    public ResponseEntity<?> updateUserDetails(@RequestBody UserRecordDTO updatedUser, HttpServletRequest request) {
    	System.out.println("                                  유저 정보 수정"
    						+"\n                                   프로필 이미지 : " + updatedUser.getProfileImage());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof CustomOAuth2User user) {
            String email = user.getUserName();
            Integer userId = userMapper.selectUserIdByEmail(email);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "사용자를 찾을 수 없습니다."));
            }

            // 업데이트할 데이터 설정
            UserRecordDTO existing = userMapper.findByUserId(userId);
            existing.setName(updatedUser.getName());
            existing.setNickname(updatedUser.getNickname());
            existing.setProfileImage(updatedUser.getProfileImage());
            existing.setAddress(updatedUser.getAddress());
            existing.setPoints(updatedUser.getPoints());
            // 업데이트 실행
            userMapper.updateUserDetails(existing);

            return ResponseEntity.ok(existing);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
            "message", "로그인이 필요합니다"
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
    public ResponseEntity<?> userlogin(@RequestBody Map<String, String> loginData, HttpServletResponse response){
    	String email = loginData.get("email");
        String password = loginData.get("password");
        System.out.println("                       로그인 진입");
    	System.out.println("                       일반 로그인 요청: " + email);
    	
    	UserRecordDTO user = userMapper.findByEmail(email);
    	if (user == null || !password.equals(user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "이메일 또는 비밀번호가 잘못되었습니다."));
        }
    	String token = jwtUtil.createJwt(
    	        user.getEmail(),
    	        user.getNickname(),
    	        user.getRole(),
    	        1000L * 60 * 60 * 2 // 2시간 (밀리초)
    	    );

    	    // ✅ accessToken 쿠키 생성
    	    Cookie cookie = new Cookie("accessToken", token);
    	    cookie.setHttpOnly(true); // JS로 접근 못하게
    	    cookie.setSecure(false); // HTTPS일 경우 true
    	    cookie.setPath("/");
    	    cookie.setMaxAge(60 * 60 * 2); // 2시간

    	    response.addCookie(cookie);
    	
    	return ResponseEntity.ok(Map.of(
    	        "userId", user.getUserId(),
    	        "email", user.getEmail(),
    	        "nickname", user.getNickname()
    	    ));
    }
    @PostMapping("/upload/media")
    public ResponseEntity<?> uploadProfileImage(
    		@RequestPart(value = "file") MultipartFile file,
    	    @RequestPart("userId") String userId) {
        System.out.println("                                                 프로필 이미지 업로드 요청된 유저 ID : " + userId);

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "파일이 없습니다."));
        }
        
        try {
            // 1. S3에 업로드하고 이미지 key(URL) 받기
            String imageUrl = s3.uploadFile(file);  // 예: "profile-images/uuid_123.jpg"
            System.out.println("                 설정된 url값"+imageUrl);
            // 2. DB에 이미지 경로 업데이트
            userMapper.updateProfileImage(imageUrl, Integer.parseInt(userId));

            // 4. 프론트에 응답
            return ResponseEntity.ok(Map.of(
                "mediaUrl", imageUrl,
                "mediaType", file.getContentType()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "이미지 업로드 실패"));
        }
        
    }
    
}
