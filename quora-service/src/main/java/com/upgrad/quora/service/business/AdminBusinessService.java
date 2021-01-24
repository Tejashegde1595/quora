package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

import static com.upgrad.quora.service.common.GenericErrorCode.*;

@Service
public class AdminBusinessService {
    private final Logger log = LoggerFactory.getLogger(AdminBusinessService.class);
    @Value("${user.admin.role}")
    private String adminRole;

    @Autowired
    private UserDao userDao;


    /** delete requested user from db if the requestor is authorized
     * @param uuid
     * @param authorizationToken
     * @throws AuthorizationFailedException
     * @throws UserNotFoundException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteUser(final String uuid,final String authorizationToken) throws AuthorizationFailedException, UserNotFoundException {
        log.debug("****** Starting deleteUser ******");
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);

        if (userAuthTokenEntity == null) {
            log.info("Invalid authorization token");
            throw new AuthorizationFailedException(ATHR_001_ADMIN.getCode(), ATHR_001_ADMIN.getDefaultMessage());
        }

        if (userAuthTokenEntity.getLogoutAt() != null || userAuthTokenEntity.getExpiresAt().isBefore(ZonedDateTime.now())) {
            log.info("User is signed out or the token is expired, logout_time: {}, expiry_time: {}",
                    userAuthTokenEntity.getLogoutAt(), userAuthTokenEntity.getExpiresAt());
            throw new AuthorizationFailedException(ATHR_002_ADMIN.getCode(), ATHR_002_ADMIN.getDefaultMessage());
        }

        final UserEntity userEntity = userDao.getUser(uuid);
        if (userEntity == null) {
            log.info("The user not found with the uuid: {} ", uuid);
            throw new UserNotFoundException(USR_001_ADMIN.getCode(), USR_001_ADMIN.getDefaultMessage());
        }
        if (!userAuthTokenEntity.getUser().getRole().equals(adminRole)) {
            log.info("This user trying to delete doesn't have admin role");
            throw new AuthorizationFailedException(ATHR_003_ADMIN.getCode(), ATHR_003_ADMIN.getDefaultMessage());
        }
        userDao.deleteUser(userEntity);
        log.debug("****** Ending deleteUser ******");

    }


}
