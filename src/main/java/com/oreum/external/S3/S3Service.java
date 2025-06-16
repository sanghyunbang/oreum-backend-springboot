package com.oreum.external.S3;
import java.io.IOException;
import java.util.UUID;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

// 여기서 실제로 S3에 파일을 올리는 역할 함!(AmasonS3Config에서 만든 객체 여기서 씀)
@Service
@RequiredArgsConstructor
public class S3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    
    // 이전 Config에서 만든 Bean이 들어감!! (RequiredArgsConstructor가 해줌)
    private final AmazonS3 amazonS3; 

    // 스프링에서 제공하는 MULTIPART 씀!! (추가 공부 해야 할듯)
    public String uploadFile(MultipartFile file) {
        // 파일명 안겹치게 하려고 하는 부분분
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            //S3에 파일정보 같이 보냄(아마존 관련 라이브러리 씀)
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            amazonS3.putObject(bucket, fileName, file.getInputStream(), metadata);
            amazonS3.putObject(bucket, fileName, file.getInputStream(), metadata);
            // bucket: 어디에 업로드할지 (폴더 아님, S3의 저장 공간)
            // fileName: S3에 저장될 파일 이름 (중복 방지됨)
            // file.getInputStream(): 실제 파일의 바이트 스트림
            // metadata: 파일의 추가 정보 (크기, 타입 등)

        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 실패", e);
        }

        // 업로드가 성공했으면, 그 파일의 S3 URL을 반환
        return amazonS3.getUrl(bucket, fileName).toString();
    }
}
