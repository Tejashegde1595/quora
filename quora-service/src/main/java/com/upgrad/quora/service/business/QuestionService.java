package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

import static com.upgrad.quora.service.common.GenericErrorCode.*;

@Service
public class QuestionService {
    private final Logger log = LoggerFactory.getLogger(QuestionService.class);
    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private UserDao userDao;

    @Value("${user.admin.role}")
    private String adminRole;

    /*
     * @param questionEntity - questionEntity Object to be persisted
     * @param authorizationToken - authorizationToken to be checked for authentication and authorization
     * @throws AuthorizationFailedException - thrown if user does not exist or is signed out
     * @returns questionEntity - questionEntity Object which is persisted
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity createQuestion(final QuestionEntity questionEntity, final String authorizationToken) throws AuthorizationFailedException {
        log.debug("****** Starting createQuestion ******");
        UserAuthTokenEntity userAuthTokenEntity = checkUserAuth(authorizationToken);
        if (userAuthTokenEntity.getLogoutAt() != null || userAuthTokenEntity.getExpiresAt().isBefore(ZonedDateTime.now())) {
            log.info("User is signed out or the token is expired, logout_time: {}, expiry_time: {}",
                    userAuthTokenEntity.getLogoutAt(), userAuthTokenEntity.getExpiresAt());
            throw new AuthorizationFailedException(ATHR_QSN_USR_001_COMMON.getCode(), ATHR_QSN_USR_001_COMMON.getDefaultMessage());
        }
        questionEntity.setUser(userAuthTokenEntity.getUser());
        final QuestionEntity question = questionDao.createQuestion(questionEntity);
        log.debug("****** Ending createQuestion ******");
        return question;
    }

    /*
     * @param authorizationToken - authorizationToken to be checked for authentication and authorization
     * @returns List<QuestionEntity> - returns list of question entities
     */
    public List<QuestionEntity> getAllQuestions(final String authorizationToken) throws AuthorizationFailedException {
        log.debug("****** Starting getAllQuestions ******");
        UserAuthTokenEntity userAuthTokenEntity = checkUserAuth(authorizationToken);
        if (userAuthTokenEntity.getLogoutAt() != null || userAuthTokenEntity.getExpiresAt().isBefore(ZonedDateTime.now())) {
            log.info("User is signed out or the token is expired, logout_time: {}, expiry_time: {}",
                    userAuthTokenEntity.getLogoutAt(), userAuthTokenEntity.getExpiresAt());
            throw new AuthorizationFailedException(ATHR_QSN_USR_002_COMMON.getCode(), ATHR_QSN_USR_002_COMMON.getDefaultMessage());
        }
        final List<QuestionEntity> questions = questionDao.getQuestions();
        log.debug("****** Ending getAllQuestions ******");
        return questions;
    }

    /*
     * @param userId - user Id for retrieving user questions
     * @param authorizationToken - authorizationToken to be checked for authentication and authorization
     * @returns List<QuestionEntity> - returns list of question entities
     */
    public List<QuestionEntity> getQuestionsByUser(final String userId, final String authorizationToken) throws AuthorizationFailedException, UserNotFoundException {
        log.debug("****** Starting getQuestionsByUser ******");
        UserAuthTokenEntity userAuthTokenEntity = checkUserAuth(authorizationToken);
        if (userAuthTokenEntity.getLogoutAt() != null || userAuthTokenEntity.getExpiresAt().isBefore(ZonedDateTime.now())) {
            log.info("User is signed out or the token is expired, logout_time: {}, expiry_time: {}",
                    userAuthTokenEntity.getLogoutAt(), userAuthTokenEntity.getExpiresAt());
            throw new AuthorizationFailedException(ATHR_QSN_USR_003_COMMON.getCode(), ATHR_QSN_USR_003_COMMON.getDefaultMessage());
        }
        if (userDao.getUser(userId) == null) {
            log.info("The user not found with the uuid: {} ", userId);
            throw new UserNotFoundException(QSN_USER_001.getCode(), QSN_USER_001.getDefaultMessage());
        }
        final List<QuestionEntity> questionsByUser = questionDao.getQuestionsByUser(userId);
        log.debug("****** Ending getQuestionsByUser ******");
        return questionsByUser;
    }

    /*
     * @param questionId - questionId Id for retrieving question to be deleted
     * @param authorizationToken - authorizationToken to be checked for authentication and authorization
     * @returns QuestionEntity - returns questionEntity which is deleted
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity deleteQuestionById(final String questionId, final String authorizationToken) throws AuthorizationFailedException, InvalidQuestionException {
        log.debug("****** Starting deleteQuestionById ******");
        UserAuthTokenEntity userAuthTokenEntity = checkUserAuth(authorizationToken);
        if (userAuthTokenEntity.getLogoutAt() != null || userAuthTokenEntity.getExpiresAt().isBefore(ZonedDateTime.now())) {
            log.info("User is signed out or the token is expired, logout_time: {}, expiry_time: {}",
                    userAuthTokenEntity.getLogoutAt(), userAuthTokenEntity.getExpiresAt());
            throw new AuthorizationFailedException(ATHR_QSN_USR_004_COMMON.getCode(), ATHR_QSN_USR_004_COMMON.getDefaultMessage());
        }
        QuestionEntity questionEntity = getQuestionByQuestionId(questionId);
        if (questionEntity.getUser() != userAuthTokenEntity.getUser() && !userAuthTokenEntity.getUser().getRole().equals(adminRole)) {
            log.info("The user trying to delete the question is neither the owner nor has admin rights");
            throw new AuthorizationFailedException(ATHR_QSN_001_COMMON.getCode(), ATHR_QSN_001_COMMON.getDefaultMessage());
        }
        final QuestionEntity entity = questionDao.deleteQuestion(questionEntity);
        log.debug("****** Ending deleteQuestionById ******");
        return entity;
    }

    /*
     * @param questionEntity - question Entity object
     * @param questionId - questionId Id for retrieving question to be deleted
     * @param authorizationToken - authorizationToken to be checked for authentication and authorization
     * @returns QuestionEntity - returns questionEntity which is deleted
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity editQuestion(final QuestionEntity questionEntity, final String questionId, final String authorizationToken) throws AuthorizationFailedException, InvalidQuestionException {
        log.debug("****** Starting editQuestion ******");
        UserAuthTokenEntity userAuthTokenEntity = checkUserAuth(authorizationToken);
        if (userAuthTokenEntity.getLogoutAt() != null || userAuthTokenEntity.getExpiresAt().isBefore(ZonedDateTime.now())) {
            log.info("User is signed out or the token is expired, logout_time: {}, expiry_time: {}",
                    userAuthTokenEntity.getLogoutAt(), userAuthTokenEntity.getExpiresAt());
            throw new AuthorizationFailedException(ATHR_QSN_USR_005_COMMON.getCode(), ATHR_QSN_USR_005_COMMON.getDefaultMessage());
        }
        QuestionEntity question = getQuestionByQuestionId(questionId);
        if (question.getUser() != userAuthTokenEntity.getUser()) {
            log.info("The user trying to modify the question is not the owner");
            throw new AuthorizationFailedException(ATHR_QSN_002_COMMON.getCode(), ATHR_QSN_002_COMMON.getDefaultMessage());
        }
        question.setContent(questionEntity.getContent());
        question.setDate(questionEntity.getDate());
        final QuestionEntity entity = questionDao.editQuestion(question);
        log.debug("****** Ending editQuestion ******");
        return entity;
    }


    /* checks user authentication based on the authorizationToken
     * @param authorizationToken - authorizationToken to be checked for authentication and authorization
     * @throws AuthorizationFailedException - thrown if user does not exist or is signed out
     * @returns userAuthToken - UserAuthTokenEntity object for the authorizationToken
     */
    private UserAuthTokenEntity checkUserAuth(final String authorizationToken) throws AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);
        if (userAuthTokenEntity == null) {
            log.info("Invalid authorization token");
            throw new AuthorizationFailedException(ATHR_001_COMMON.getCode(), ATHR_001_COMMON.getDefaultMessage());
        }
        return userAuthTokenEntity;
    }

    /*
     * @param questionId
     * @returns questionEntity
     */
    private QuestionEntity getQuestionByQuestionId(final String questionId) throws InvalidQuestionException {
        QuestionEntity question = questionDao.getQuestionById(questionId);
        if (question == null) {
            log.info("The question not found with the uuid: {} ", questionId);
            throw new InvalidQuestionException(QSN_001.getCode(), QSN_001.getDefaultMessage());
        }
        return question;
    }

}
