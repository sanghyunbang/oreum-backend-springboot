package com.oreum.map.mountains.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oreum.map.mountains.DTO.MountainDTO;

import jakarta.annotation.PostConstruct;

@Service
public class MountainService {

    private final List<MountainDTO> mountainList = new ArrayList<>();

    @PostConstruct
    public void loadMountains() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("data/mountains.json");
        if (inputStream == null) {
            throw new IOException(" [!] mountains.json 파일을 찾을 수 없습니다.");
        }

        MountainDTO[] mountains = mapper.readValue(inputStream, MountainDTO[].class);
        mountainList.addAll(Arrays.asList(mountains));
        System.out.println("[O] mountains.json 로드 완료: " + mountainList.size() + "개 지점");
    }

    public List<MountainDTO> getAll() {
        return mountainList;
    }

    public Optional<MountainDTO> findByNum(int mountainNum) {
        return mountainList.stream()
                .filter(m -> m.getMountainNum() == mountainNum)
                .findFirst();
    }

    public List<MountainDTO> searchByName(String keyword) {
        return mountainList.stream()
                .filter(m -> m.getName().contains(keyword))
                .limit(10)
                .collect(Collectors.toList());
    }
}
