package com.fauzi.jobservice.controller;

import com.fauzi.jobservice.apisecurity.annotation.ApiScope;
import com.fauzi.jobservice.apisecurity.annotation.ApiScopes;
import com.fauzi.jobservice.apisecurity.model.LoginScopes;
import com.fauzi.jobservice.apisecurity.model.ScopeType;
import com.fauzi.jobservice.model.response.DansJobListResponse;
import com.fauzi.jobservice.service.DansService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DansController {
    private DansService dansService;

    public DansController(DansService dansService) {
        this.dansService = dansService;
    }

    @GetMapping(value = "/v1/job-list")
    @ApiScopes({
            @ApiScope(scope = {LoginScopes.LOGIN}, type = ScopeType.CUSTOMER)
    })
    public DansJobListResponse[] jobListResponse(){
        return dansService.getJobList();
    }

    @GetMapping(value = "/v1/job-detail/{id}")
    @ApiScopes({
            @ApiScope(scope = {LoginScopes.LOGIN}, type = ScopeType.CUSTOMER)
    })
    public DansJobListResponse jobDetailResponse(@PathVariable String id){
        return dansService.getJobDetail(id);
    }
}
