package com.oreum.map.mountains.Controller;

import org.springframework.web.bind.annotation.RestController;

import com.oreum.map.mountains.DTO.MountainDTO;
import com.oreum.map.mountains.Service.MountainService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/mountains")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class mountainController {

    private final MountainService mountainService;

    @GetMapping("/search")
    public List<MountainDTO> search(@RequestParam("query") String query) {
        System.out.println("검색 실행");
        return mountainService.searchByName(query);
    }
}
