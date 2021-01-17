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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.upgrad.quora.service.common.GenericErrorCode.*;

@SuppressWarnings("DuplicatedCode")
@Service
public class AnswerBusinessService {

    @Autowired
    private AnswerDao answerDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private QuestionDao questionDao;

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
        UserAuthTokenEntity userAuthTokenEntity = checkUserAuth(authorization);
        QuestionEntity questionEntity = questionDao.getQuestionById(questionId);
        if(questionEntity == null) {
            throw new InvalidQuestionException(QUES_001.getCode(), QUES_001.getDefaultMessage());
        }
        answerEntity.setQuestion_Id(questionEntity);
        answerEntity.setUser_Id(userAuthTokenEntity.getUser());
        return answerDao.createAnswer(answerEntity);
    }

    /**
     * Business logic to edit an existing answer
     *
     * @param authorization
     * @param answerId
     * @param answerEntity
     * @return updated answer
     * @throws AuthorizationFailedException
     * @throws AnswerNotFoundException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity editAnswer(final String authorization, final String answerId, final AnswerEntity answerEntity) throws AuthorizationFailedException, AnswerNotFoundException {
        UserAuthTokenEntity userAuthToken = checkUserAuth(authorization);
        AnswerEntity existingAnswer = answerDao.getAnswerById(answerId);
        if(existingAnswer == null) {
            throw new AnswerNotFoundException(ANS_USER_001.getCode(), ANS_USER_001.getDefaultMessage());
        }
        if(existingAnswer.getUser_Id() != userAuthToken.getUser()) {
            throw new AuthorizationFailedException(ATHR_003_COMMON.getCode(), ATHR_003_COMMON.getDefaultMessage());
        }
        existingAnswer.setAnswer(answerEntity.toString());
        answerDao.updateAnswer(existingAnswer);
        return existingAnswer;
    }

    public AnswerEntity deleteAnswer(final String answerId, final String authorization) throws AuthorizationFailedException, AnswerNotFoundException {
        UserAuthTokenEntity userAuthToken = checkUserAuth(authorization);
        AnswerEntity existingAnswer = answerDao.getAnswerById(answerId);
        if(existingAnswer == null) {
            throw new AnswerNotFoundException(ANS_USER_001.getCode(), ANS_USER_001.getDefaultMessage());
        }
        if(existingAnswer.getUser_Id()!=userAuthToken.getUser() || userAuthToken.getUser().getRole().equals("admin")) {
            throw new AuthorizationFailedException(ATHR_003_COMMON.getCode(), ATHR_003_COMMON.getDefaultMessage());
        }
        return answerDao.deleteAnswer(answerId);
    }

    public List<AnswerEntity> getAllAnswersToQuestion(final String questionId, final String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        checkUserAuth(authorization);
        AnswerEntity answerEntity = answerDao.getQuestionId(questionId);
        if (answerEntity == null) {
            throw new InvalidQuestionException("QUES-001", "The question with entered uuid whose details are to be seen does not exist");
        }
        return answerDao.getAllAnswersToQuestion(questionId);
    }

    private UserAuthTokenEntity checkUserAuth(final String authorizationToken) throws AuthorizationFailedException{
        UserAuthTokenEntity userAuthTokenEntity= userDao.getUserAuthToken(authorizationToken);
        if(userAuthTokenEntity == null) {// || userAuthTokenEntity.getExpiresAt().isBefore(ZonedDateTime.now())) {
            throw new AuthorizationFailedException(ATHR_001_COMMON.getCode(), ATHR_001_COMMON.getDefaultMessage());
        }
        else if(userAuthTokenEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException(ATHR_002_COMMON.getCode(), ATHR_002_COMMON.getDefaultMessage());
        }
        return userAuthTokenEntity;
    }
}
