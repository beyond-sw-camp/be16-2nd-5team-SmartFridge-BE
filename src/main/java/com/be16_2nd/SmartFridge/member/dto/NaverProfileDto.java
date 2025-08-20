package com.be16_2nd.SmartFridge.member.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverProfileDto {

    private Response response;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response{
        private String id;
        private String nickname;
        private String email;
        private String profile_image;
    }
}
