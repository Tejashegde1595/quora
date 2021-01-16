package com.upgrad.quora.service.business;

import com.upgrad.quora.service.common.Constants;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Base64;

import static com.upgrad.quora.service.common.GenericErrorCode.ATH_001;
import static com.upgrad.quora.service.common.GenericErrorCode.ATH_002;

@Service
public class SigninAuthenticationService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;


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
}
