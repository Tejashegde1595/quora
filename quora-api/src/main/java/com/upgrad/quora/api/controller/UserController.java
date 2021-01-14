package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.SigninResponse;
import com.upgrad.quora.api.model.SignoutResponse;
import com.upgrad.quora.api.model.SignupUserRequest;
import com.upgrad.quora.api.model.SignupUserResponse;
import com.upgrad.quora.service.business.AuthenticationService;
import com.upgrad.quora.service.business.SignoutBusinessService;
import com.upgrad.quora.service.business.SignupBusinessService;
import com.upgrad.quora.service.business.UserAdminBusinessService;
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

import java.util.Base64;
import java.util.UUID;

import static com.upgrad.quora.service.common.GenericErrorCode.ATH_001;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserAdminBusinessService userAdminBusinessService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private SignupBusinessService signupBusinessService;
    @Autowired
    private SignoutBusinessService signoutBusinessService;

    @Value("${user.default.role}")
    private String defaultRole;

    @RequestMapping(method = RequestMethod.POST, path = "/signup", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupUserResponse> userSignup(final SignupUserRequest signupUserRequest) throws SignUpRestrictedException {
        final UserEntity userEntity = convertToUserEntity(signupUserRequest);
        final UserEntity createdUserEntity = signupBusinessService.signup(userEntity);
        SignupUserResponse userResponse = new SignupUserResponse().id(createdUserEntity.getUuid()).status(Constants.USER_REGISTRATION_MESSAGE);
        return new ResponseEntity<SignupUserResponse>(userResponse, HttpStatus.CREATED);
    }


    @RequestMapping(method = RequestMethod.POST, path = "/signin", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SigninResponse> signin(@RequestHeader("authorization") final String authorization) throws AuthenticationFailedException {

        try {
            byte[] decode = Base64.getDecoder().decode(authorization.split(Constants.HEADER_STRING)[1]);
            String decodedText = new String(decode);
            String[] decodedArray = decodedText.split(":");

            UserAuthTokenEntity userAuthToken = authenticationService.authenticate(decodedArray[0], decodedArray[1]);

            SigninResponse signinResponse = new SigninResponse().id(userAuthToken.getUuid()).message(Constants.LOGIN_MESSAGE);

            HttpHeaders headers = new HttpHeaders();
            headers.add("access-token", userAuthToken.getAccessToken());
            return new ResponseEntity<SigninResponse>(signinResponse, headers, HttpStatus.OK);
        } catch (Exception e) {
            throw new AuthenticationFailedException(ATH_001.getCode(), ATH_001.getDefaultMessage());
        }
    }

    @RequestMapping(method = RequestMethod.POST, path = "/signout", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignoutResponse> signout(@RequestHeader("authorization") final String authorization) throws SignOutRestrictedException {

        String[] bearerToken = authorization.split(Constants.TOKEN_PREFIX);
        if (bearerToken.length != 2) {
            throw new SignOutRestrictedException("SGR-001", "User is not Signed in");
        }
        final UserEntity user = signoutBusinessService.signoutUser(bearerToken[1]);

        SignoutResponse signoutResponse = new SignoutResponse().id(user.getUuid()).message(Constants.LOGOUT_MESSAGE);
        return new ResponseEntity<SignoutResponse>(signoutResponse, HttpStatus.OK);
    }

    private UserEntity convertToUserEntity(final SignupUserRequest signupUserRequest) {
        UserEntity userEntity = modelMapper.map(signupUserRequest, UserEntity.class);
        userEntity.setUuid(UUID.randomUUID().toString());
        userEntity.setRole(defaultRole);
        return userEntity;

    }
}
