package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
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
    public QuestionEntity createQuestion(final QuestionEntity questionEntity,final String AuthorizationToken) throws AuthorizationFailedException{
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(AuthorizationToken);
        if(userAuthTokenEntity==null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        else if(userAuthTokenEntity.getLogoutAt()!=null || ZonedDateTime.now().isAfter(userAuthTokenEntity.getExpiresAt())){
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to post a question");
        }else{
            questionEntity.setUser(userAuthTokenEntity.getUser());
        }
        return questionDao.createQuestion(questionEntity);
    }

    public List<QuestionEntity> getAllQuestions(final String AuthorizationToken) throws AuthorizationFailedException{
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(AuthorizationToken);
        if(userAuthTokenEntity==null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        else if(userAuthTokenEntity.getLogoutAt()!=null || ZonedDateTime.now().isAfter(userAuthTokenEntity.getExpiresAt())){
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to post a question");
        }
        return questionDao.getQuestions();
    }

    public List<QuestionEntity> getQuestionsByUser(final String userId,final String AuthorizationToken) throws AuthorizationFailedException,UserNotFoundException{
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(AuthorizationToken);
        if(userAuthTokenEntity==null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        else if(userAuthTokenEntity.getLogoutAt()!=null || ZonedDateTime.now().isAfter(userAuthTokenEntity.getExpiresAt())){
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to post a question");
        }else if(userDao.getUser(userId)==null){
            throw new UserNotFoundException("USR-001","User with entered uuid whose question details are to be seen does not exist");
        }
        return questionDao.getQuestionsByUser(userId);
    }


}
