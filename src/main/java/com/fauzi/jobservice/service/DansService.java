package com.fauzi.jobservice.service;

import com.fauzi.jobservice.adaptor.DansServiceAdaptor;
import com.fauzi.jobservice.model.response.DansJobListResponse;
import org.springframework.stereotype.Service;

@Service
public class DansService {
    private DansServiceAdaptor dansServiceAdaptor;

    public DansService(DansServiceAdaptor dansServiceAdaptor) {
        this.dansServiceAdaptor = dansServiceAdaptor;
    }

    public DansJobListResponse[] getJobList() {
        return dansServiceAdaptor.jobListReq();
    }

    public DansJobListResponse getJobDetail(String id) {
        return dansServiceAdaptor.jobDetailReq(id);
    }
}
