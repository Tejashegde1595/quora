package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
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
        UserAuthTokenEntity userAuthTokenEntity = checkUserAuth(authorizationToken);
        if (userAuthTokenEntity.getLogoutAt() != null || userAuthTokenEntity.getExpiresAt().isBefore(ZonedDateTime.now())) {
            throw new AuthorizationFailedException(ATHR_QSN_USR_001_COMMON.getCode(), ATHR_QSN_USR_001_COMMON.getDefaultMessage());
        }
        questionEntity.setUser(userAuthTokenEntity.getUser());
        return questionDao.createQuestion(questionEntity);
    }

    /*
     * @param authorizationToken - authorizationToken to be checked for authentication and authorization
     * @returns List<QuestionEntity> - returns list of question entities
     */
    public List<QuestionEntity> getAllQuestions(final String authorizationToken) throws AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity = checkUserAuth(authorizationToken);
        if (userAuthTokenEntity.getLogoutAt() != null || userAuthTokenEntity.getExpiresAt().isBefore(ZonedDateTime.now())) {
            throw new AuthorizationFailedException(ATHR_QSN_USR_002_COMMON.getCode(), ATHR_QSN_USR_002_COMMON.getDefaultMessage());
        }
        return questionDao.getQuestions();
    }

    /*
     * @param userId - user Id for retrieving user questions
     * @param authorizationToken - authorizationToken to be checked for authentication and authorization
     * @returns List<QuestionEntity> - returns list of question entities
     */
    public List<QuestionEntity> getQuestionsByUser(final String userId, final String authorizationToken) throws AuthorizationFailedException, UserNotFoundException {
        UserAuthTokenEntity userAuthTokenEntity = checkUserAuth(authorizationToken);
        if (userAuthTokenEntity.getLogoutAt() != null || userAuthTokenEntity.getExpiresAt().isBefore(ZonedDateTime.now())) {
            throw new AuthorizationFailedException(ATHR_QSN_USR_003_COMMON.getCode(), ATHR_QSN_USR_003_COMMON.getDefaultMessage());
        }
        if (userDao.getUser(userId) == null) {
            throw new UserNotFoundException(QSN_USER_001.getCode(), QSN_USER_001.getDefaultMessage());
        }
        return questionDao.getQuestionsByUser(userId);
    }

    /*
     * @param questionId - questionId Id for retrieving question to be deleted
     * @param authorizationToken - authorizationToken to be checked for authentication and authorization
     * @returns QuestionEntity - returns questionEntity which is deleted
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity deleteQuestionById(final String questionId, final String authorizationToken) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthTokenEntity userAuthTokenEntity = checkUserAuth(authorizationToken);
        if (userAuthTokenEntity.getLogoutAt() != null || userAuthTokenEntity.getExpiresAt().isBefore(ZonedDateTime.now())) {
            throw new AuthorizationFailedException(ATHR_QSN_USR_004_COMMON.getCode(), ATHR_QSN_USR_004_COMMON.getDefaultMessage());
        }
        QuestionEntity questionEntity = getQuestionByQuestionId(questionId);
        if (questionEntity.getUser() != userAuthTokenEntity.getUser() && !userAuthTokenEntity.getUser().getRole().equals(adminRole)) {
            throw new AuthorizationFailedException(ATHR_QSN_001_COMMON.getCode(), ATHR_QSN_001_COMMON.getDefaultMessage());
        }
        return questionDao.deleteQuestion(questionEntity);
    }

    /*
     * @param questionEntity - question Entity object
     * @param questionId - questionId Id for retrieving question to be deleted
     * @param authorizationToken - authorizationToken to be checked for authentication and authorization
     * @returns QuestionEntity - returns questionEntity which is deleted
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity editQuestion(final QuestionEntity questionEntity, final String questionId, final String authorizationToken) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthTokenEntity userAuthTokenEntity = checkUserAuth(authorizationToken);
        if (userAuthTokenEntity.getLogoutAt() != null || userAuthTokenEntity.getExpiresAt().isBefore(ZonedDateTime.now())) {
            throw new AuthorizationFailedException(ATHR_QSN_USR_005_COMMON.getCode(), ATHR_QSN_USR_005_COMMON.getDefaultMessage());
        }
        QuestionEntity question = getQuestionByQuestionId(questionId);
        if (question.getUser() != userAuthTokenEntity.getUser()) {
            throw new AuthorizationFailedException(ATHR_QSN_002_COMMON.getCode(), ATHR_QSN_002_COMMON.getDefaultMessage());
        }
        question.setContent(questionEntity.getContent());
        question.setDate(questionEntity.getDate());
        return questionDao.editQuestion(question);
    }


     /* checks user authentication based on the authorizationToken
     * @param authorizationToken - authorizationToken to be checked for authentication and authorization
     * @throws AuthorizationFailedException - thrown if user does not exist or is signed out
     * @returns userAuthToken - UserAuthTokenEntity object for the authorizationToken
     */
    private UserAuthTokenEntity checkUserAuth(final String authorizationToken) throws AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);
        if (userAuthTokenEntity == null) {
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
            throw new InvalidQuestionException(QSN_001.getCode(), QSN_001.getDefaultMessage());
        }
        return question;
    }

}
