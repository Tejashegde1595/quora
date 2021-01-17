package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.QuestionEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
public class QuestionDao {
    @PersistenceContext
    private EntityManager entityManager;

    /*
    * @param questionEntity
    * @returns questionEntity
    */
    public QuestionEntity createQuestion(QuestionEntity questionEntity){
        entityManager.persist(questionEntity);
        return questionEntity;
    }

    /*
    * @returns List<QuestionEntity>
    */
    public List<QuestionEntity> getQuestions(){
        TypedQuery<QuestionEntity> query = entityManager.createQuery("Select q from QuestionEntity q",QuestionEntity.class);
        List<QuestionEntity> questionList = query.getResultList();
        return questionList;
    }
    /*
     * @params userId
     * @returns List<QuestionEntity>
     */
    public List<QuestionEntity> getQuestionsByUser(String userId){
        return  entityManager.createNamedQuery("questionByUserId",QuestionEntity.class).setParameter("uuid",userId).getResultList();
    }
    /*
     * @params questionId
     * @returns QuestionEntity
     */
    public QuestionEntity getQuestionById(String questionId){
        try {
            return entityManager.createNamedQuery("questionById", QuestionEntity.class).setParameter("uuid", questionId).getSingleResult();
        }catch (NoResultException nre){
            return null;
        }
    }
    /*
     * @params questionEntity
     * @returns questionEntity
     */
    public QuestionEntity deleteQuestion(QuestionEntity questionEntity){
        entityManager.remove(questionEntity);
        return questionEntity;
    }
    /*
     * @params questionEntity
     * @returns questionEntity
     */
    public QuestionEntity editQuestion(QuestionEntity questionEntity){
        entityManager.merge(questionEntity);
        return questionEntity;
    }
}
