package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.UserDetailsResponse;
import com.upgrad.quora.service.business.UserCommonBusinessService;
import com.upgrad.quora.service.common.Constants;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.upgrad.quora.service.common.GenericErrorCode.*;

@RestController
public class CommonController {
    @Autowired
    UserCommonBusinessService userCommonBusinessService;

    @Autowired
    ModelMapper modelMapper;

    @RequestMapping(method = RequestMethod.GET, path = "/userprofile/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDetailsResponse> getUser(@PathVariable("id") final String userUuid,
                                                       @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, UserNotFoundException {
        String bearerToken = "";
        try {
            bearerToken = authorization.split(Constants.TOKEN_PREFIX)[1];
        } catch (Exception e) {
            throw new AuthorizationFailedException(ATHR_001_COMMON.getCode(), ATHR_001_COMMON.getDefaultMessage());
        }

        final UserEntity userEntity = userCommonBusinessService.getUser(userUuid, bearerToken);
        UserDetailsResponse userDetailsResponse = convertToDto(userEntity);
        return new ResponseEntity<UserDetailsResponse>(userDetailsResponse, HttpStatus.OK);
    }

    private UserDetailsResponse convertToDto(UserEntity userEntity) {
        UserDetailsResponse userDetailsResponse = modelMapper.map(userEntity, UserDetailsResponse.class);
        return userDetailsResponse;
    }
}
