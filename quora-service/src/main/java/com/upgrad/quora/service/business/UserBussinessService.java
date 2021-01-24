package com.upgrad.quora.service.business;

import com.upgrad.quora.service.common.Constants;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Base64;

import static com.upgrad.quora.service.common.GenericErrorCode.*;

@Service
public class UserBussinessService {
    private final Logger log = LoggerFactory.getLogger(UserBussinessService.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;

    /**
     * Business logic to create an user based on sign-up request details
     *
     * @param userEntity
     * @return
     * @throws SignUpRestrictedException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity signup(final UserEntity userEntity) throws SignUpRestrictedException {
        log.debug("****** Starting signup ******");
        validateUserDetails(userEntity);
        String[] encryptedText = cryptographyProvider.encrypt(userEntity.getPassword());
        userEntity.setSalt(encryptedText[0]);
        userEntity.setPassword(encryptedText[1]);
        UserEntity user = userDao.createUser(userEntity);
        log.debug("****** Ending signup ******");
        return user;

    }

    /**
     * Business logic for signing-in an user based on authentication
     *
     * @param authorization
     * @return
     * @throws AuthenticationFailedException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthTokenEntity authenticate(final String authorization) throws AuthenticationFailedException {
        log.debug("****** Starting authenticate ******");
        String username = "";
        String password = "";
        try {
            byte[] decode = Base64.getDecoder().decode(authorization.split(Constants.HEADER_STRING)[1]);
            String decodedText = new String(decode);
            String[] decodedArray = decodedText.split(":");
            username = decodedArray[0];
            password = decodedArray[1];
        } catch (Exception e) {
            log.error("Invalid authentication token format");
            throw new AuthenticationFailedException(ATH_001.getCode(), ATH_001.getDefaultMessage());

        }
        UserEntity userEntity = userDao.getUserByUserName(username);
        if (userEntity == null) {
            log.info("This username {} does not exist",username);
            throw new AuthenticationFailedException(ATH_001.getCode(), ATH_001.getDefaultMessage());
        }
        UserAuthTokenEntity userAuthToken=null;
        final String encryptedPassword = cryptographyProvider.encrypt(password, userEntity.getSalt());
        if (encryptedPassword.equals(userEntity.getPassword())) {
            log.info("Password validation successful for userName: {}",username);
             userAuthToken = createUserAuthToken(userEntity, encryptedPassword);
        } else {
            log.info("Invalid password for userName: {}",username);
            throw new AuthenticationFailedException(ATH_002.getCode(), ATH_002.getDefaultMessage());
        }
        log.debug("****** Ending authenticate ******");
        return userAuthToken;
    }

    /**
     * to create an user auth-token
     *
     * @param userEntity
     * @param secret
     * @return
     */
    private UserAuthTokenEntity createUserAuthToken(UserEntity userEntity, String secret) {
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(secret);
        UserAuthTokenEntity userAuthToken = new UserAuthTokenEntity();
        userAuthToken.setUser(userEntity);
        final ZonedDateTime now = ZonedDateTime.now();
        final ZonedDateTime expiresAt = now.plusHours(Constants.EXPIRATION_TIME);
        userAuthToken.setAccessToken(jwtTokenProvider.generateToken(userEntity.getUuid(), now, expiresAt));
        userAuthToken.setLoginAt(now);
        userAuthToken.setExpiresAt(expiresAt);
        userAuthToken.setUuid(userEntity.getUuid());
        userDao.createAuthToken(userAuthToken);
        log.debug("auth-token successfully created for userName {}", userEntity.getUserName());
        return userAuthToken;
    }

    /**
     * Business logic to logout an already signed in user
     *
     * @param authorization
     * @return
     * @throws SignOutRestrictedException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity signoutUser(final String authorization) throws SignOutRestrictedException {
        UserAuthTokenEntity userAuthToken = getUserAuthToken(authorization);

        if (userAuthToken.getLogoutAt() != null) {
            log.info("User is already signed out at: {}", userAuthToken.getLogoutAt());
            throw new SignOutRestrictedException(SGOR_001.getCode(), SGOR_001.getDefaultMessage());
        }
        userAuthToken.setLogoutAt(ZonedDateTime.now());
        userAuthToken = userDao.updateAuthToken(userAuthToken);

        return userAuthToken.getUser();
    }


    /**
     * To fetch user auth-token details
     *
     * @param authorizationToken
     * @return
     * @throws SignOutRestrictedException
     */
    private UserAuthTokenEntity getUserAuthToken(final String authorizationToken) throws
            SignOutRestrictedException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);
        if (userAuthTokenEntity == null) {
            log.info("Invalid authorization token");
            throw new SignOutRestrictedException(SGOR_001.getCode(), SGOR_001.getDefaultMessage());
        }
        return userAuthTokenEntity;
    }


    /**
     * method to validate user data for sign-up request
     *
     * @param user
     * @throws SignUpRestrictedException
     */
    private void validateUserDetails(final UserEntity user) throws
            SignUpRestrictedException {

        UserEntity userEntity = userDao.getUserByUserName(user.getUserName());
        if (userEntity != null) {
            log.info("This username {} is already taken", user.getUserName());
            throw new SignUpRestrictedException(SGUR_001.getCode(), SGUR_001.getDefaultMessage());
        }

        userEntity = userDao.getUserByEmail(user.getEmailAddress());
        if (userEntity != null) {
            log.info("This email {} is already registered", user.getEmailAddress());
            throw new SignUpRestrictedException(SGUR_002.getCode(), SGUR_002.getDefaultMessage());
        }

    }
}

