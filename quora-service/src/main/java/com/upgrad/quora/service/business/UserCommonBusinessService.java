package com.upgrad.quora.service.business;

import com.upgrad.quora.service.common.Constants;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

import static com.upgrad.quora.service.common.GenericErrorCode.*;

@Service
public class UserCommonBusinessService {
    @Autowired
    UserDao userDao;

    public UserEntity getUser(String uuid, String authorizationToken) throws AuthorizationFailedException, UserNotFoundException {

        String bearerToken = "";
        try {
            bearerToken = authorizationToken.split(Constants.TOKEN_PREFIX)[1];
        } catch (Exception e) {
            throw new AuthorizationFailedException(ATHR_001_COMMON.getCode(), ATHR_001_COMMON.getDefaultMessage());
        }

        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(bearerToken);
        /*  UserEntity user = userAuthTokenEntity.getUser();
            if (userAuthTokenEntity == null || !user.getUuid().equals(uuid)) {*/
        if (userAuthTokenEntity == null || userAuthTokenEntity.getExpiresAt().isBefore(ZonedDateTime.now())) {
            throw new AuthorizationFailedException(ATHR_001_COMMON.getCode(), ATHR_001_COMMON.getDefaultMessage());
        }

        if (userAuthTokenEntity.getLogoutAt() != null && userAuthTokenEntity.getLogoutAt().isAfter(userAuthTokenEntity.getLoginAt())) {
            throw new AuthorizationFailedException(ATHR_002_COMMON.getCode(), ATHR_002_COMMON.getDefaultMessage());
        }
        UserEntity userEntity = userDao.getUser(uuid);
        if (userEntity == null) {
            throw new UserNotFoundException(USR_001_COMMON.getCode(), USR_001_COMMON.getDefaultMessage());
        }

        return userEntity;
    }

}
