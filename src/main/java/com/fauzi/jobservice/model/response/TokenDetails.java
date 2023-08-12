package com.fauzi.jobservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenDetails {
    private String accessToken;
    private ExpiryDetails expiryDetails;
}

