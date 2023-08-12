package com.fauzi.jobservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostLoginUserResponse {
    private String accessToken;
    private String refreshToken;
    private String validity;
    private String scope;
}

