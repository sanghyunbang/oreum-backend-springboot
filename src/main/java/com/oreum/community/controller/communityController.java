package com.oreum.community.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oreum.auth.dto.CustomOAuth2User;
import com.oreum.auth.mapper.UserDao;
import com.oreum.community.dto.FeednameDTO;
import com.oreum.community.dto.communityDTO;
import com.oreum.community.mapper.communityMapper;
import com.oreum.posts.dto.MyFeedDTO;
import com.oreum.posts.dto.PostsDTO;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true") //  CORS 허용

public class communityController {

    private final communityMapper _comm;
    private final UserDao _um;
    
    @GetMapping("/{communityName}")
    public ResponseEntity<communityDTO> getCommunityName(@PathVariable("communityName") String communityName) {
        System.out.println("		커뮤이름 호출 들어옴");
        communityDTO dto = _comm.getCommunity(communityName);
        return ResponseEntity.ok(dto);
    }
    
    @PostMapping("/insertpost")
    public ResponseEntity<?> insertcom(@RequestBody communityDTO dto, Authentication authentication) {
        
    	System.out.println("insert 들어온 데이터 : " + dto);
    	if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        // OAuth2 로그인 사용자가 맞는지 확인
        if (authentication.getPrincipal() instanceof CustomOAuth2User user) {
            // 로그인 사용자 이메일에서 사용자 정보 추출
            String email = user.getUserName();
            System.out.println("	겟 유저네임 : "+email);
            int creatorId = _um.selectUserIdByEmail(email);
            System.out.println("	작성자 id : " + creatorId);
            String nickname = _um.userNameByuserId(creatorId);
            System.out.println("	닉네임 : " + nickname);

            // dto에 필요한 값 세팅
            dto.setCreatorId(creatorId);
            dto.setCreatorNickname(nickname);
            dto.setCreatedAt(LocalDateTime.now());
            dto.setUpdatedAt(LocalDateTime.now());

            _comm.insertCommunity(dto);

            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(400).body("잘못된 사용자 정보입니다.");
    }

    @GetMapping("/list")
    public ResponseEntity<List<communityDTO>> getCommunityList() {
    	System.out.println("		커뮤니티 리스트 호출됨");
        List<communityDTO> list = _comm.getAllCommunities();
        return ResponseEntity.ok(list);
    }
    
    @PostMapping("/createfeed")
    public ResponseEntity<?> createCustomFeed(@RequestBody MyFeedDTO request, Authentication auth) {
    	System.out.println("                                 커스텀피드 생성 유저 정보 : " + auth);
        if (auth == null || !auth.isAuthenticated()) {
        	System.out.println("                 로그인 인증 X");
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        if (!(auth.getPrincipal() instanceof CustomOAuth2User user)) {
            return ResponseEntity.status(400).body("잘못된 사용자 정보입니다.");
        }
        
        String feedname = request.getFeedname();
        int userId = _um.selectUserIdByEmail(user.getUserName());
        System.out.println("                                 피드이름 : " + feedname + 
        					"\n                                 유저id : " + userId);
        request.setUserId(userId);
        _comm.insertFeed(userId, feedname);
        
        int feedId = _comm.getFeedNameById(feedname);
        System.out.println("                                 피드 Id 값 : " + feedId);
        
        List<Integer> boardIds = request.getBoardIdList();
        System.out.println("                                 보드 배열 : "+boardIds);
        
        if (boardIds != null && !boardIds.isEmpty()) {
            for (int boardId : boardIds) {
                _comm.insertFeedBoard(feedId, userId, boardId);
            }
        } else {
            return ResponseEntity.badRequest().body("커뮤니티 선택이 필요합니다.");
        }

        return ResponseEntity.ok("맞춤 피드가 성공적으로 생성되었습니다.");
    }
    
    @PostMapping("/myfeeds")
    public ResponseEntity<?> getMyFeeds(Authentication auth){
    	System.out.println("                             피드목록 불러오기");
    	if (!(auth.getPrincipal() instanceof CustomOAuth2User user)) {
            return ResponseEntity.status(400).body("잘못된 사용자 정보입니다.");
        }
    	String email = user.getUserName();
    	int userId = _um.selectUserIdByEmail(email);
    	
    	List<MyFeedDTO> myFeeds = _comm.getFeedsByUserId(userId);
    	System.out.println("\n                  마이피드 리스트의 값 : "+myFeeds +"\n");
    	return ResponseEntity.ok(myFeeds);    	
    }
    @GetMapping("/feeds/{feedname}")
    public ResponseEntity<?> getFeedName(@PathVariable("feedname") String feedname) {
        System.out.println("\n         	피드 호출 들어옴    \n");
        int userId = _comm.getFeedIdByuserId(feedname);
        List<MyFeedDTO> boards = _comm.getBoardIdsByuserId(userId);
        
        List<PostsDTO> allPosts = new ArrayList<>();
        for (MyFeedDTO board : boards) {
            int boardId = board.getBoardId();
            List<PostsDTO> posts = _comm.getBoardIdByFindposts(boardId);
            allPosts.addAll(posts);
        }
        return ResponseEntity.ok(allPosts);
    }

}