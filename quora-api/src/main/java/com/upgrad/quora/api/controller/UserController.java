package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.SigninResponse;
import com.upgrad.quora.api.model.SignoutResponse;
import com.upgrad.quora.api.model.SignupUserRequest;
import com.upgrad.quora.api.model.SignupUserResponse;
import com.upgrad.quora.service.business.UserBussinessService;
import com.upgrad.quora.service.common.Constants;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private UserBussinessService userBussinessService;

    @Value("${user.default.role}")
    private String defaultRole;

    /**
     * @param signupUserRequest
     * @return
     * @throws SignUpRestrictedException
     */
    @RequestMapping(method = RequestMethod.POST, path = "/signup", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupUserResponse> userSignup(final SignupUserRequest signupUserRequest) throws SignUpRestrictedException {
        final UserEntity userEntity = convertToUserEntity(signupUserRequest);
        final UserEntity createdUserEntity = userBussinessService.signup(userEntity);
        SignupUserResponse userResponse = new SignupUserResponse().id(createdUserEntity.getUuid()).status(Constants.USER_REGISTRATION_MESSAGE);
        return new ResponseEntity<SignupUserResponse>(userResponse, HttpStatus.CREATED);
    }


    /**
     * @param authorization
     * @return
     * @throws AuthenticationFailedException
     */
    @RequestMapping(method = RequestMethod.POST, path = "/signin", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SigninResponse> signin(@RequestHeader("authorization") final String authorization) throws AuthenticationFailedException {


        UserAuthTokenEntity userAuthToken = userBussinessService.authenticate(authorization);

        SigninResponse signinResponse = new SigninResponse().id(userAuthToken.getUuid()).message(Constants.LOGIN_MESSAGE);

        HttpHeaders headers = new HttpHeaders();
        headers.add("access-token", userAuthToken.getAccessToken());
        return new ResponseEntity<SigninResponse>(signinResponse, headers, HttpStatus.OK);

    }

    /**
     * @param authorization
     * @return
     * @throws SignOutRestrictedException
     */
    @RequestMapping(method = RequestMethod.POST, path = "/signout", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignoutResponse> signout(@RequestHeader("authorization") final String authorization) throws SignOutRestrictedException {

        final UserEntity user = userBussinessService.signoutUser(authorization);

        SignoutResponse signoutResponse = new SignoutResponse().id(user.getUuid()).message(Constants.LOGOUT_MESSAGE);
        return new ResponseEntity<SignoutResponse>(signoutResponse, HttpStatus.OK);
    }

    /**
     * @param signupUserRequest
     * @return
     */
    private UserEntity convertToUserEntity(final SignupUserRequest signupUserRequest) {
        UserEntity userEntity = modelMapper.map(signupUserRequest, UserEntity.class);
        userEntity.setUuid(UUID.randomUUID().toString());
        userEntity.setRole(defaultRole);
        return userEntity;

    }
}
