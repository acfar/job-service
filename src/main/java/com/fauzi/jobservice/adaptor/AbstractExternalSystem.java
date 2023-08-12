package com.fauzi.jobservice.adaptor;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public abstract class AbstractExternalSystem {
    protected RestTemplate restTemplate;

    protected AbstractExternalSystem(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected <T> ResponseEntity<T> call(
            String url,
            HttpMethod httpMethod,
            Object request,
            HttpHeaders httpHeaders,
            Class<T> responseClass) {
        return restTemplate.exchange(
                url, httpMethod, new HttpEntity<>(request, httpHeaders), responseClass);
    }
}
