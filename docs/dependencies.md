# Spring Boot Dependencies (for oreum-backend-springboot)

## ✅ Core
- **Spring Web**  
  > HTTP 요청 처리 및 RESTful API 개발을 위한 기본 웹 프레임워크

- **Spring Security**  
  > 인증 및 권한 부여를 위한 보안 프레임워크

- **OAuth2 Client**  
  > Kakao, Naver, Google 등 외부 로그인 연동을 위한 OAuth2 클라이언트

## ✅ 데이터 계층
- **MyBatis Framework (선택 예정)**  
  > JPA 대신 사용할 SQL 기반 ORM 프레임워크  
  ✅ 아직 선택하지 않았지만, 이후 수동으로 추가 가능

- **MySQL Driver**  
  > MySQL DB와 연결하기 위한 JDBC 드라이버

- **Spring Data Redis (Access+Driver)**  
  > 동기 방식으로 Redis에 접근할 수 있게 해주는 모듈 (세션 캐싱, 토큰 저장 등)

## ✅ 메일 서비스
- **Spring Boot Starter Mail**  
  > JavaMailSender를 사용한 이메일 발송 기능 구현

## ❌ 제외된 항목
- **Spring Boot DevTools**: 개발 편의 기능 (자동 재시작 등), **선택하지 않음**
- **Spring Data JPA**: MyBatis 사용 예정이므로 **선택하지 않음**

---

## ✨ 향후 필요할 수 있는 추가 의존성

| 목적             | 의존성 추천                         |
|------------------|-------------------------------------|
| NoSQL DB 사용     | Spring Data MongoDB                |
| 배치 처리         | Spring Batch                       |
| 스케줄링          | Spring Task (또는 @Scheduled 사용) |
| 인증 토큰 처리    | jjwt, nimbus-jose-jwt, etc.        |

---

> ⚙️ 위 목록은 `build.gradle` 또는 `pom.xml`에 맞춰 반영될 수 있으며, `MyBatis`는 직접 수동 설정이 필요합니다.
