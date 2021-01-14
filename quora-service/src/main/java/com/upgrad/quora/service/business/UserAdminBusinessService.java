package com.upgrad.quora.service.business;


import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAdminBusinessService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;

    public UserEntity getUser(final String userUuid, final String authorizationToken) throws
            UserNotFoundException {


        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);
        //RoleEntity role = userAuthTokenEntity.getUser().getRole();
        // if (role != null && role.equals("admin")) {}
        UserEntity userEntity = userDao.getUser(userUuid);
        if (userEntity == null) {
            throw new UserNotFoundException("USR-001", "User not found");
        }
        return userEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity createUser(final UserEntity userEntity) throws SignUpRestrictedException {
        validateUserDetails(userEntity);
        String password = userEntity.getPassword();
        if (password == null) {
            userEntity.setPassword("quora@123");
        }
        String[] encryptedText = cryptographyProvider.encrypt(userEntity.getPassword());
        userEntity.setSalt(encryptedText[0]);
        userEntity.setPassword(encryptedText[1]);
        return userDao.createUser(userEntity);

    }

    public void validateUserDetails(final UserEntity user) throws
            SignUpRestrictedException {

        //UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);
        //RoleEntity role = userAuthTokenEntity.getUser().getRole();
        UserEntity userEntity = userDao.getUserByUserName(user.getUserName());
        if (userEntity != null) {
            throw new SignUpRestrictedException("SGR-001", "Try any other Username, this Username has already been taken");
        }

        userEntity = userDao.getUserByEmail(user.getEmailAddress());
        if (userEntity != null) {
            throw new SignUpRestrictedException("SGR-002", "This user has already been registered, try with any other emailId");
        }


        // }
        //throw new UnauthorizedException("ATH-002", "you are not authorized to fetch user details");
    }

}

