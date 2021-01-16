package com.upgrad.quora.service.business;

import com.upgrad.quora.service.common.GenericErrorCode;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import static com.upgrad.quora.service.common.GenericErrorCode.*;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class QuestionService {
    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private UserDao userDao;

    @Value("${user.default.role}")
    private String role;

    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity createQuestion(final QuestionEntity questionEntity,final String authorizationToken) throws AuthorizationFailedException{
        UserAuthTokenEntity userAuthTokenEntity = checkUserAuth(authorizationToken);
        questionEntity.setUser(userAuthTokenEntity.getUser());
        return questionDao.createQuestion(questionEntity);
    }

    public List<QuestionEntity> getAllQuestions(final String authorizationToken) throws AuthorizationFailedException{
        checkUserAuth(authorizationToken);
        return questionDao.getQuestions();
    }

    public List<QuestionEntity> getQuestionsByUser(final String userId,final String authorizationToken) throws AuthorizationFailedException,UserNotFoundException{
        checkUserAuth(authorizationToken);
        if(userDao.getUser(userId)==null){
            throw new UserNotFoundException(QSN_USER_001.getCode(), QSN_USER_001.getDefaultMessage());
        }
        return questionDao.getQuestionsByUser(userId);
    }
    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity deleteQuestionById(final String questionId,final String authorizationToken) throws AuthorizationFailedException, InvalidQuestionException{
        UserAuthTokenEntity userAuthTokenEntity = checkUserAuth(authorizationToken);
        QuestionEntity questionEntity = getQuestionByQuestionId(questionId);
        if(questionEntity.getUser()!=userAuthTokenEntity.getUser() && userAuthTokenEntity.getUser().getRole().equals(role)){
            throw new AuthorizationFailedException(ATHR_QSN_001_COMMON.getCode(),ATHR_QSN_001_COMMON.getDefaultMessage());
        }
        return questionDao.deleteQuestion(questionEntity);
    }
    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity editQuestion(final QuestionEntity questionEntity,final String questionId,final String authorizationToken)throws AuthorizationFailedException, InvalidQuestionException{
        UserAuthTokenEntity userAuthTokenEntity = checkUserAuth(authorizationToken);
        QuestionEntity question = getQuestionByQuestionId(questionId);
        if(question.getUser()!=userAuthTokenEntity.getUser()){
            throw new AuthorizationFailedException(ATHR_QSN_002_COMMON.getCode(),ATHR_QSN_002_COMMON.getDefaultMessage());
        }
        question.setContent(questionEntity.getContent());
        question.setDate(questionEntity.getDate());
        return questionDao.editQuestion(question);
    }

    private UserAuthTokenEntity checkUserAuth(final String authorizationToken) throws AuthorizationFailedException{
        UserAuthTokenEntity userAuthTokenEntity= userDao.getUserAuthToken(authorizationToken);
        if(userAuthTokenEntity==null) {
            throw new AuthorizationFailedException(ATHR_001_COMMON.getCode(), ATHR_001_COMMON.getDefaultMessage());
        }else if(userAuthTokenEntity.getLogoutAt()!=null){
            throw new AuthorizationFailedException(ATHR_002_COMMON.getCode(), ATHR_002_COMMON.getDefaultMessage());
        }
        return userAuthTokenEntity;
    }

    private QuestionEntity getQuestionByQuestionId(final String questionId) throws InvalidQuestionException{
        QuestionEntity question = questionDao.getQuestionById(questionId);
        if(question==null){
            throw new InvalidQuestionException(QSN_001.getCode(), QSN_001.getDefaultMessage());
        }
        return question;
    }

}
