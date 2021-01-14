package com.upgrad.quora.service.business;


import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.upgrad.quora.service.common.GenericErrorCode.SGR_001;
import static com.upgrad.quora.service.common.GenericErrorCode.SGR_002;

@Service
public class SignupBusinessService {

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


    private void validateUserDetails(final UserEntity user) throws
            SignUpRestrictedException {

        UserEntity userEntity = userDao.getUserByUserName(user.getUserName());
        if (userEntity != null) {
            throw new SignUpRestrictedException(SGR_001.getCode(), SGR_001.getDefaultMessage());
        }

        userEntity = userDao.getUserByEmail(user.getEmailAddress());
        if (userEntity != null) {
            throw new SignUpRestrictedException(SGR_002.getCode(), SGR_002.getDefaultMessage());
        }

    }
}

