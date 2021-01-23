package com.upgrad.quora.service.business;

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
public class CommonBusinessService {
    @Autowired
    UserDao userDao;

    /** Business logic to fetch user details
     * @param uuid
     * @param authorizationToken
     * @return
     * @throws AuthorizationFailedException
     * @throws UserNotFoundException
     */
    public UserEntity getUser(final String uuid, final String authorizationToken) throws AuthorizationFailedException, UserNotFoundException {

        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);

        if (userAuthTokenEntity == null) {
            throw new AuthorizationFailedException(ATHR_001_COMMON.getCode(), ATHR_001_COMMON.getDefaultMessage());
        }

        if (userAuthTokenEntity.getLogoutAt() != null || userAuthTokenEntity.getExpiresAt().isBefore(ZonedDateTime.now())) {
            throw new AuthorizationFailedException(ATHR_002_COMMON.getCode(), ATHR_002_COMMON.getDefaultMessage());
        }
        UserEntity userEntity = userDao.getUser(uuid);
        if (userEntity == null) {
            throw new UserNotFoundException(USR_001_COMMON.getCode(), USR_001_COMMON.getDefaultMessage());
        }

        return userEntity;
    }

}
