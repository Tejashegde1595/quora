package com.upgrad.quora.service.business;

import com.upgrad.quora.service.common.Constants;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

import static com.upgrad.quora.service.common.GenericErrorCode.SGOR_001;

@Service
public class SignoutBusinessService {
    @Autowired
    private UserDao userDao;


    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity signoutUser(final String authorization) throws SignOutRestrictedException {
        String[] bearerToken = authorization.split(Constants.TOKEN_PREFIX);
        if (bearerToken.length != 2) {
            throw new SignOutRestrictedException(SGOR_001.getCode(), SGOR_001.getDefaultMessage());
        }
        UserAuthTokenEntity userAuthToken = getUserAuthToken(bearerToken[1]);
        userAuthToken.setLogoutAt(ZonedDateTime.now());
        userAuthToken = userDao.updateAuthToken(userAuthToken);

        return userAuthToken.getUser();
    }


    private UserAuthTokenEntity getUserAuthToken(final String authorizationToken) throws
            SignOutRestrictedException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);
        if (userAuthTokenEntity == null) {
            throw new SignOutRestrictedException(SGOR_001.getCode(), SGOR_001.getDefaultMessage());
        }
        return userAuthTokenEntity;
    }

}
