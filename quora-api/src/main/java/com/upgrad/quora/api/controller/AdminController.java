package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.UserDeleteResponse;
import com.upgrad.quora.service.business.UserAdminBusinessService;
import com.upgrad.quora.service.common.Constants;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserAdminBusinessService userAdminBusinessService;

    @RequestMapping(method = RequestMethod.DELETE, value = "/user/{userId}")
    public ResponseEntity<UserDeleteResponse> deleteUser(@RequestHeader("authorization") final String authorization,
                                                         @PathVariable("userId") final String uuid) throws AuthorizationFailedException, UserNotFoundException {
        userAdminBusinessService.deleteUser(uuid, authorization);
        UserDeleteResponse userResponse = new UserDeleteResponse().id(uuid).status(Constants.DELETE_USER_MESSAGE);
        return new ResponseEntity<UserDeleteResponse>(userResponse, HttpStatus.OK);
    }


}

