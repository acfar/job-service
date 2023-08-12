package com.fauzi.jobservice.service;

import com.fauzi.jobservice.configuration.exception.GeneralException;
import com.fauzi.jobservice.model.entity.User;
import com.fauzi.jobservice.model.request.PostLoginUserRequest;
import com.fauzi.jobservice.model.response.PostLoginUserResponse;
import com.fauzi.jobservice.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

import static com.fauzi.jobservice.util.TokenUtils.generateToken;

@Service
@Log4j2
public class UserService {

    @Value("${api-security.global.kid}")
    private String kid;

    @Value("${api-security.global.private-key}")
    private String privateKeyValue;

    @Value("${api-security.global.public-key}")
    private String publicKeyValue;

    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public PostLoginUserResponse login(PostLoginUserRequest request){
        User userData = userRepository.findByUsername(request.getUsername()).orElseThrow(
                ()-> {
                    log.error("Get Error when find username");
                    throw new GeneralException(HttpStatus.CONFLICT,"40009", "Error when find username in database");
                });
        if(BCrypt.checkpw(request.getPassword(),userData.getPassword())){
            var tokenDetails = generateToken(userData.getUsername(),userData.getEmail(),kid,privateKeyValue,publicKeyValue);
            var refreshToken = UUID.randomUUID().toString();
            userRepository.save(userData);
            return PostLoginUserResponse.builder()
                    .accessToken(Objects.requireNonNull(tokenDetails.getAccessToken()))
                    .refreshToken(Objects.requireNonNull(refreshToken))
                    .validity("60")
                    .scope("login")
                    .build();
        }
        throw new GeneralException(HttpStatus.UNAUTHORIZED, "40001","Please check your password and username");
    }

}
