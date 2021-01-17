package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.UserDetailsResponse;
import com.upgrad.quora.service.business.CommonBusinessService;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class CommonController {
    @Autowired
    CommonBusinessService commonBusinessService;

    @Autowired
    ModelMapper modelMapper;

    /**
     * @param userUuid
     * @param authorization
     * @return
     * @throws AuthorizationFailedException
     * @throws UserNotFoundException
     */
    @RequestMapping(method = RequestMethod.GET, path = "/userprofile/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDetailsResponse> getUser(@PathVariable("id") final String userUuid,
                                                       @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, UserNotFoundException {

        final UserEntity userEntity = commonBusinessService.getUser(userUuid, authorization);
        UserDetailsResponse userDetailsResponse = convertToDto(userEntity);
        return new ResponseEntity<UserDetailsResponse>(userDetailsResponse, HttpStatus.OK);
    }

    private UserDetailsResponse convertToDto(UserEntity userEntity) {
        UserDetailsResponse userDetailsResponse = modelMapper.map(userEntity, UserDetailsResponse.class);
        return userDetailsResponse;
    }
}
