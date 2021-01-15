package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
@Service
public class SignoutBusinessService {
    @Autowired
    private UserDao userDao;


    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity signoutUser(final String token) throws SignOutRestrictedException {
        UserAuthTokenEntity userAuthToken = getUserAuthToken(token);
        userAuthToken.setLogoutAt(ZonedDateTime.now());
        userAuthToken = userDao.updateAuthToken(userAuthToken);

        return userAuthToken.getUser();
    }


    public UserAuthTokenEntity getUserAuthToken(final String authorizationToken) throws
            SignOutRestrictedException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);
        if (userAuthTokenEntity == null) {
            throw new SignOutRestrictedException("SGR-001", "User is not Signed in");
        }
        return userAuthTokenEntity;
    }

}
