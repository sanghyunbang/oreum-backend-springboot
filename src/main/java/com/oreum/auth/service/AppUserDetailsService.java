package com.oreum.auth.service;

import com.oreum.auth.dto.AppUserRecordDTO;
import com.oreum.auth.mapper.AppUserDao;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserDao appUserDao;

    public AppUserDetailsService(AppUserDao appUserDao) {
        this.appUserDao = appUserDao;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("AppUserDetailsService: '" + email + "' 이메일로 사용자 정보 로드 시도.");

        AppUserRecordDTO userRecord = appUserDao.findByEmail(email);

        if (userRecord == null) {
            System.out.println("AppUserDetailsService: 이메일 '" + email + "' 에 해당하는 사용자를 찾을 수 없습니다.");
            throw new UsernameNotFoundException("이메일 " + email + " 에 해당하는 사용자를 찾을 수 없습니다.");
        }

        String passwordHash = userRecord.getPasswordHash();

        // --- 여기부터 상세 디버깅 로그 추가 ---
        System.out.println("AppUserDetailsService DEBUG: DB 조회 후 userRecord 객체: " + userRecord.toString()); // DTO 전체 내용 출력 (보안상 민감 정보 주의)
        System.out.println("AppUserDetailsService DEBUG: DB 조회 후 userRecord.getPasswordHash() 값: " + (passwordHash != null ? passwordHash : "NULL"));
        System.out.println("AppUserDetailsService DEBUG: passwordHash.isEmpty(): " + (passwordHash != null ? passwordHash.isEmpty() : "N/A"));
        System.out.println("AppUserDetailsService DEBUG: passwordHash.trim().isEmpty(): " + (passwordHash != null ? passwordHash.trim().isEmpty() : "N/A"));
        // --- 여기까지 상세 디버깅 로그 ---

        if (passwordHash == null || passwordHash.trim().isEmpty()) {
            System.out.println("AppUserDetailsService: 사용자 '" + email + "'의 비밀번호 해시가 유효하지 않습니다 (null 또는 empty).");
            throw new BadCredentialsException("사용자 " + email + " 의 비밀번호가 올바르게 설정되지 않았습니다.");
        }
        
        System.out.println("AppUserDetailsService: 사용자 '" + email + "' 찾음. 저장된 비밀번호 해시 길이: " + passwordHash.length());
        
        return new User(
            userRecord.getEmail(),
            passwordHash,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userRecord.getRole().toUpperCase()))
        );
    }
}