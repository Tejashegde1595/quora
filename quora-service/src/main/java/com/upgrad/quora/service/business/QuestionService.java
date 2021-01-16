package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class QuestionService {
    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private UserDao userDao;

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
            throw new UserNotFoundException("USR-001","User with entered uuid whose question details are to be seen does not exist");
        }
        return questionDao.getQuestionsByUser(userId);
    }
    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity deleteQuestionById(final String questionId,final String authorizationToken) throws AuthorizationFailedException, InvalidQuestionException{
        UserAuthTokenEntity userAuthTokenEntity = checkUserAuth(authorizationToken);
        QuestionEntity questionEntity = getQuestionByQuestionId(questionId);
        if(questionEntity.getUser()!=userAuthTokenEntity.getUser() && userAuthTokenEntity.getUser().getRole().equals("nonadmin")){
            throw new AuthorizationFailedException("ATHR-003","Only the question owner or admin can delete the question");
        }
        return questionDao.deleteQuestion(questionEntity);
    }
    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity editQuestion(final QuestionEntity questionEntity,final String questionId,final String authorizationToken)throws AuthorizationFailedException, InvalidQuestionException{
        UserAuthTokenEntity userAuthTokenEntity = checkUserAuth(authorizationToken);
        QuestionEntity question = getQuestionByQuestionId(questionId);
        if(question.getUser()!=userAuthTokenEntity.getUser()){
            throw new AuthorizationFailedException("ATHR-003","Only the question owner can edit the question");
        }
        question.setContent(questionEntity.getContent());
        question.setDate(questionEntity.getDate());
        return questionDao.editQuestion(question);
    }

    private UserAuthTokenEntity checkUserAuth(final String authorizationToken) throws AuthorizationFailedException{
        UserAuthTokenEntity userAuthTokenEntity= userDao.getUserAuthToken(authorizationToken);
        if(userAuthTokenEntity==null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }else if(userAuthTokenEntity.getLogoutAt()!=null || ZonedDateTime.now().isAfter(userAuthTokenEntity.getExpiresAt())){
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to post a question");
        }
        return userAuthTokenEntity;
    }

    private QuestionEntity getQuestionByQuestionId(final String questionId) throws InvalidQuestionException{
        QuestionEntity question = questionDao.getQuestionById(questionId);
        if(question==null){
            throw new InvalidQuestionException("QUES-001","Entered question uuid does not exist");
        }
        return question;
    }

}
