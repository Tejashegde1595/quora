package com.upgrad.quora.service.business;

import com.upgrad.quora.service.common.Constants;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Base64;

import static com.upgrad.quora.service.common.GenericErrorCode.*;

@Service
public class UserBussinessService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;

    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity signup(final UserEntity userEntity) throws SignUpRestrictedException {
        validateUserDetails(userEntity);
        String[] encryptedText = cryptographyProvider.encrypt(userEntity.getPassword());
        userEntity.setSalt(encryptedText[0]);
        userEntity.setPassword(encryptedText[1]);
        return userDao.createUser(userEntity);

    }

    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthTokenEntity authenticate(final String authorization) throws AuthenticationFailedException {

        String username = "";
        String password = "";
        try {
            byte[] decode = Base64.getDecoder().decode(authorization.split(Constants.HEADER_STRING)[1]);
            String decodedText = new String(decode);
            String[] decodedArray = decodedText.split(":");
            username = decodedArray[0];
            password = decodedArray[1];
        } catch (Exception e) {
            throw new AuthenticationFailedException(ATH_001.getCode(), ATH_001.getDefaultMessage());

        }
        UserEntity userEntity = userDao.getUserByUserName(username);
        if (userEntity == null) {
            throw new AuthenticationFailedException(ATH_001.getCode(), ATH_001.getDefaultMessage());
        }

        final String encryptedPassword = cryptographyProvider.encrypt(password, userEntity.getSalt());
        if (encryptedPassword.equals(userEntity.getPassword())) {
            JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
            UserAuthTokenEntity userAuthToken = new UserAuthTokenEntity();
            userAuthToken.setUser(userEntity);
            final ZonedDateTime now = ZonedDateTime.now();
            final ZonedDateTime expiresAt = now.plusHours(Constants.EXPIRATION_TIME);
            userAuthToken.setAccessToken(jwtTokenProvider.generateToken(userEntity.getUuid(), now, expiresAt));
            userAuthToken.setLoginAt(now);
            userAuthToken.setExpiresAt(expiresAt);
            userAuthToken.setUuid(userEntity.getUuid());
            userDao.createAuthToken(userAuthToken);

            return userAuthToken;
        } else {
            throw new AuthenticationFailedException(ATH_002.getCode(), ATH_002.getDefaultMessage());
        }

    }

    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity signoutUser(final String authorization) throws SignOutRestrictedException {
        UserAuthTokenEntity userAuthToken = getUserAuthToken(authorization);
        if (userAuthToken.getLogoutAt() != null) {
            throw new SignOutRestrictedException(SGOR_001.getCode(), SGOR_001.getDefaultMessage());
        }
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


    private void validateUserDetails(final UserEntity user) throws
            SignUpRestrictedException {

        UserEntity userEntity = userDao.getUserByUserName(user.getUserName());
        if (userEntity != null) {
            throw new SignUpRestrictedException(SGUR_001.getCode(), SGUR_001.getDefaultMessage());
        }

        userEntity = userDao.getUserByEmail(user.getEmailAddress());
        if (userEntity != null) {
            throw new SignUpRestrictedException(SGUR_002.getCode(), SGUR_002.getDefaultMessage());
        }

    }
}

