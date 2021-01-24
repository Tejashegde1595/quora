package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.QuestionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
public class QuestionDao {
    private final Logger log = LoggerFactory.getLogger(QuestionDao.class);
    @PersistenceContext
    private EntityManager entityManager;

    /*
    * @param questionEntity
    * @returns questionEntity
    */
    public QuestionEntity createQuestion(QuestionEntity questionEntity){
        log.info("creating a new question in the database");
        entityManager.persist(questionEntity);
        return questionEntity;
    }

    /*
    * @returns List<QuestionEntity>
    */
    public List<QuestionEntity> getQuestions(){
        log.info("getting all questions from the database");
        TypedQuery<QuestionEntity> query = entityManager.createQuery("Select q from QuestionEntity q",QuestionEntity.class);
        List<QuestionEntity> questionList = query.getResultList();
        return questionList;
    }
    /*
     * @params userId
     * @returns List<QuestionEntity>
     */
    public List<QuestionEntity> getQuestionsByUser(String userId){
        log.info("getting all questions from the database based on the User Id");
        return  entityManager.createNamedQuery("questionByUserId",QuestionEntity.class).setParameter("uuid",userId).getResultList();
    }
    /*
     * @params questionId
     * @returns QuestionEntity
     */
    public QuestionEntity getQuestionById(String questionId){
        log.info("getting question from the database based on the Question Id");
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
        log.info("deleting a question from the database");
        entityManager.remove(questionEntity);
        return questionEntity;
    }
    /*
     * @params questionEntity
     * @returns questionEntity
     */
    public QuestionEntity editQuestion(QuestionEntity questionEntity){
        log.info("updating a question in the database");
        entityManager.merge(questionEntity);
        return questionEntity;
    }
}
