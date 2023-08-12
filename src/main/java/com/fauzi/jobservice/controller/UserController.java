package com.fauzi.jobservice.controller;

import com.fauzi.jobservice.apisecurity.annotation.ApiScope;
import com.fauzi.jobservice.apisecurity.annotation.ApiScopes;
import com.fauzi.jobservice.apisecurity.model.LoginScopes;
import com.fauzi.jobservice.apisecurity.model.ScopeType;
import com.fauzi.jobservice.model.request.PostLoginUserRequest;
import com.fauzi.jobservice.model.response.PostLoginUserResponse;
import com.fauzi.jobservice.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/v1/login")
    @ApiScopes({
            @ApiScope(scope = {LoginScopes.PUBLIC}, type = ScopeType.CUSTOMER)
    })
    public PostLoginUserResponse loginUserResponse(@RequestBody PostLoginUserRequest request){
        return userService.login(request);
    }

}

