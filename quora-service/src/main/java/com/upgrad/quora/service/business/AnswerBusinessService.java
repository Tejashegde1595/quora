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

import static com.upgrad.quora.service.common.GenericErrorCode.*;

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
    public AnswerEntity createAnswer(AnswerEntity answerEntity, final String authorization, final String questionId) throws UserNotFoundException, InvalidQuestionException {
        UserAuthTokenEntity userAuthToken = userDao.getUserAuthToken(authorization);
        if(userAuthToken == null) {
            throw new UserNotFoundException(ATHR_001_COMMON.getCode(), ATHR_001_COMMON.getDefaultMessage());
        }
        else if(userAuthToken.getLogoutAt() != null) {
            throw new UserNotFoundException(ATHR_002_COMMON.getCode(), ATHR_002_COMMON.getDefaultMessage());
        }

        QuestionEntity questionEntity = questionDao.getQuestion(questionId);
        if(questionEntity == null) {
            throw new InvalidQuestionException(QUES_001.getCode(), QUES_001.getDefaultMessage());
        }

        answerEntity.setQuestion(questionEntity);
        answerEntity.setUser(userAuthToken.getUser());
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
    public AnswerEntity editAnswer(final String authorization, final String answerId, final String answerEntity) throws AuthorizationFailedException, AnswerNotFoundException {
        UserAuthTokenEntity userAuthToken = userDao.getUserAuthToken(authorization);
        if(userAuthToken == null) {
            throw new AuthorizationFailedException(ATHR_001_COMMON.getCode(), ATHR_001_COMMON.getDefaultMessage());
        }
        else if(userAuthToken.getLogoutAt() != null) {
            throw new AuthorizationFailedException(ATHR_002_COMMON.getCode(), ATHR_002_COMMON.getDefaultMessage());
        }

        AnswerEntity existingAnswer = answerDao.getAnswerById(answerId);
        if(existingAnswer == null) {
            throw new AnswerNotFoundException(ANS_001.getCode(), ANS_001.getDefaultMessage());
        }

        if(!existingAnswer.getUser().getUuid().equals(userAuthToken.getUser().getUuid())) {
            throw new AuthorizationFailedException(ATHR_003_COMMON.getCode(), ATHR_003_ADMIN.getDefaultMessage());
        }

        existingAnswer.setAnswer(answerEntity);
        answerDao.updateAnswer(existingAnswer);

        return existingAnswer;
    }
}
