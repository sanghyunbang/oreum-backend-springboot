package com.oreum.goods.dto;



import lombok.Data; // Lombok @Data 어노테이션 임포트

// 아임포트 웹훅 콜백 데이터를 매핑하기 위한 DTO
@Data // getter, setter, toString, equals, hashCode 등을 자동으로 생성
public class IamportCallbackDTO {
    private String imp_uid;
    private String merchant_uid;
    private boolean success; // 결제 성공 여부 (아임포트가 판단한 초기 결과)
    private String error_msg; // 실패 시 에러 메시지
    private String custom_data;
}