package com.fauzi.jobservice.adaptor;

import com.fauzi.jobservice.model.response.DansJobListResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@Component
@Log4j2
public class DansServiceAdaptor extends AbstractExternalSystem{

    @Value("${dans.job.list.url}")
    private String jobListUrl;
    @Value("${dans.job.detail.url}")
    private String jobDetailUrl;

    protected DansServiceAdaptor(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public DansJobListResponse[] jobListReq() {
        HttpHeaders httpHeaders = new HttpHeaders();


        ResponseEntity<DansJobListResponse[]> response =
                call(
                        jobListUrl,
                        HttpMethod.GET,
                        "",
                        httpHeaders,
                        DansJobListResponse[].class);


        return response.getBody();
    }

    public DansJobListResponse jobDetailReq(String id) {
        HttpHeaders httpHeaders = new HttpHeaders();

        String url = jobDetailUrl+id;
        ResponseEntity<DansJobListResponse> response =
                call(
                        url,
                        HttpMethod.GET,
                        "",
                        httpHeaders,
                        DansJobListResponse.class);


        return response.getBody();
    }
}
