package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.QuestionEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
public class QuestionDao {
    @PersistenceContext
    private EntityManager entityManager;

    public QuestionEntity createQuestion(QuestionEntity questionEntity){
        entityManager.persist(questionEntity);
        return questionEntity;
    }

    public List<QuestionEntity> getQuestions(){
        TypedQuery<QuestionEntity> query = entityManager.createQuery("Select q from QuestionEntity q",QuestionEntity.class);
        List<QuestionEntity> questionList = query.getResultList();
        return questionList;
    }

    public List<QuestionEntity> getQuestionsByUser(String userId){
        return  entityManager.createNamedQuery("questionByUserId",QuestionEntity.class).setParameter("uuid",userId).getResultList();
    }
}
