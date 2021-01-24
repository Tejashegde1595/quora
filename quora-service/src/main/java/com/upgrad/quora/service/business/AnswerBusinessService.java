package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.AnswerDao;
import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
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
public class AnswerBusinessService {
    private final Logger log = LoggerFactory.getLogger(AnswerBusinessService.class);
    @Autowired
    private AnswerDao answerDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private QuestionDao questionDao;

    @Value("${user.admin.role}")
    private String adminRole;

    /**
     * Business Logic to create a new answer
     *
     * @param answerEntity
     * @param authorization
     * @param questionId
     * @return new answer created
     * @throws UserNotFoundException
     * @throws InvalidQuestionException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity createAnswer(AnswerEntity answerEntity, final String authorization, final String questionId) throws InvalidQuestionException, AuthorizationFailedException {
        log.debug("****** Starting createAnswer ******");
        UserAuthTokenEntity userAuthTokenEntity = checkUserAuth(authorization);
        if (userAuthTokenEntity.getLogoutAt() != null || userAuthTokenEntity.getExpiresAt().isBefore(ZonedDateTime.now())) {
            log.info("User is signed out or the token is expired, logout_time: {}, expiry_time: {}",
                    userAuthTokenEntity.getLogoutAt(), userAuthTokenEntity.getExpiresAt());
            throw new AuthorizationFailedException(ATHR_002_CREATE_ANS.getCode(), ATHR_002_CREATE_ANS.getDefaultMessage());
        }
        QuestionEntity questionEntity = questionDao.getQuestionById(questionId);
        if (questionEntity == null) {
            log.info("The question not found with the uuid: {} ", questionId);
            throw new InvalidQuestionException(QUES_001.getCode(), QUES_001.getDefaultMessage());
        }
        answerEntity.setQuestion(questionEntity);
        answerEntity.setUser(userAuthTokenEntity.getUser());
        final AnswerEntity answer = answerDao.createAnswer(answerEntity);
        log.debug("****** Ending createAnswer ******");
        return answer;
    }

    /**
     * Business logic to edit an existing answer
     *
     * @param authorization
     * @param answerId
     * @param answerContent
     * @return updated answer
     * @throws AuthorizationFailedException
     * @throws AnswerNotFoundException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity editAnswer(final String authorization, final String answerId, final String answerContent) throws AuthorizationFailedException, AnswerNotFoundException {
        log.debug("****** Starting editAnswer ******");
        UserAuthTokenEntity userAuthToken = checkUserAuth(authorization);
        if (userAuthToken.getLogoutAt() != null || userAuthToken.getExpiresAt().isBefore(ZonedDateTime.now())) {
            log.info("User is signed out or the token is expired, logout_time: {}, expiry_time: {}",
                    userAuthToken.getLogoutAt(), userAuthToken.getExpiresAt());
            throw new AuthorizationFailedException(ATHR_002_EDIT_ANS.getCode(), ATHR_002_EDIT_ANS.getDefaultMessage());
        }
        AnswerEntity existingAnswer = answerDao.getAnswerById(answerId);
        if (existingAnswer == null) {
            log.info("answer not found with the uuid: {} ", answerId);
            throw new AnswerNotFoundException(ANS_USER_001.getCode(), ANS_USER_001.getDefaultMessage());
        }
        if (existingAnswer.getUser() != userAuthToken.getUser()) {
            log.info("The user trying to modify the answer is not the owner");
            throw new AuthorizationFailedException(ATHR_003_COMMON.getCode(), ATHR_003_COMMON.getDefaultMessage());
        }
        existingAnswer.setAnswer(answerContent);
        answerDao.updateAnswer(existingAnswer);
        log.debug("****** Ending editAnswer ******");
        return existingAnswer;
    }

    /**
     * Business logic to delete an answer
     *
     * @param answerId
     * @param authorization
     * @return Deleted answer object
     * @throws AuthorizationFailedException
     * @throws AnswerNotFoundException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity deleteAnswer(final String answerId, final String authorization) throws AuthorizationFailedException, AnswerNotFoundException {
        log.debug("****** Starting editAnswer ******");
        UserAuthTokenEntity userAuthToken = checkUserAuth(authorization);
        if (userAuthToken.getLogoutAt() != null || userAuthToken.getExpiresAt().isBefore(ZonedDateTime.now())) {
            log.info("User is signed out or the token is expired, logout_time: {}, expiry_time: {}",
                    userAuthToken.getLogoutAt(), userAuthToken.getExpiresAt());
            throw new AuthorizationFailedException(ATHR_002_DEL_ANS.getCode(), ATHR_002_DEL_ANS.getDefaultMessage());
        }
        AnswerEntity existingAnswer = answerDao.getAnswerById(answerId);
        if (existingAnswer == null) {
            log.info("The answer not found with the uuid: {} ", answerId);
            throw new AnswerNotFoundException(ANS_USER_001.getCode(), ANS_USER_001.getDefaultMessage());
        }
        if (existingAnswer.getUser() != userAuthToken.getUser() && !userAuthToken.getUser().getRole().equals(adminRole)) {
            log.info("The user trying to delete the answer is neither the owner nor has admin rights");
            throw new AuthorizationFailedException(ATHR_004_COMMON.getCode(), ATHR_004_COMMON.getDefaultMessage());
        }
        log.debug("****** Starting deleteAnswer ******");
        return answerDao.deleteAnswer(answerId);
    }

    /**
     * Business logic to fetch all answers for a question
     *
     * @param questionId
     * @param authorization
     * @return List of all answers
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     */
    public List<AnswerEntity> getAllAnswersToQuestion(final String questionId, final String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        log.debug("****** Starting getAllAnswersToQuestion ******");
        checkUserAuth(authorization);
        UserAuthTokenEntity userAuthToken = checkUserAuth(authorization);
        if (userAuthToken.getLogoutAt() != null || userAuthToken.getExpiresAt().isBefore(ZonedDateTime.now())) {
            log.info("User is signed out or the token is expired, logout_time: {}, expiry_time: {}",
                    userAuthToken.getLogoutAt(), userAuthToken.getExpiresAt());
            throw new AuthorizationFailedException(ATHR_002_GET_ANS.getCode(), ATHR_002_GET_ANS.getDefaultMessage());
        }
        QuestionEntity question = questionDao.getQuestionById(questionId);
        if (question == null) {
            log.info("The question not found with the uuid: {} ", questionId);
            throw new InvalidQuestionException(INVALID_QUES_001_GET_ANS.getCode(), INVALID_QUES_001_GET_ANS.getDefaultMessage());
        }
        log.debug("****** Ending getAllAnswersToQuestion ******");
        return answerDao.getAllAnswersToQuestion(question);
    }

    /**
     * Private method to verify user authentication token
     *
     * @param authorizationToken
     * @return Answer entity
     * @throws AuthorizationFailedException
     */
    private UserAuthTokenEntity checkUserAuth(final String authorizationToken) throws AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);
        if (userAuthTokenEntity == null) {
            log.info("Invalid authorization token");
            throw new AuthorizationFailedException(ATHR_001_COMMON.getCode(), ATHR_001_COMMON.getDefaultMessage());
        }
        return userAuthTokenEntity;
    }
}
