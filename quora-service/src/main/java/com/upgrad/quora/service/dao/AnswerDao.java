package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.business.AnswerBusinessService;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class AnswerDao {

    private final Logger log = LoggerFactory.getLogger(AnswerDao.class);

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Function to persist answer into db
     *
     * @param answerEntity
     * @return answer inserted in db
     */
    public AnswerEntity createAnswer(AnswerEntity answerEntity) {
        log.info("creating a new answer in the database");
        entityManager.persist(answerEntity);
        log.info("Succesfully created a new answer in the database");
        return answerEntity;
    }

    /**
     * Method to fetch answer from db on basis of answerId
     *
     * @param answerId
     * @return answer present in db
     */
    public AnswerEntity getAnswerById(String answerId) {
        log.info("getting an answer from the database by Id");
        try {
            return entityManager.createNamedQuery("getAnswerById", AnswerEntity.class).setParameter("uuid", answerId).getSingleResult();
        }
        catch (NoResultException nre) {
            log.info("answer with id not present");
            return null;
        }
    }

    /**
     * Method to update and existing answer in db
     *
     * @param answerEntity
     */
    public void updateAnswer(AnswerEntity answerEntity) {
        log.info("updating an answer in the database");
        entityManager.merge(answerEntity);
        log.info("succesfully updated an answer in the database");
    }

    /**
     * Method to delete an answer from db
     *
     * @param answerId
     * @return deleted answer object
     */
    public AnswerEntity deleteAnswer(final String answerId) {
        log.info("deleting an answer in the database");
        AnswerEntity deleteAnswer = getAnswerById(answerId);
        if (deleteAnswer != null) {
            entityManager.remove(deleteAnswer);
            log.info("successfully deleted an answer in the database");
        }
        return deleteAnswer;
    }

    /**
     * Method to retrieve all answers for a question from db
     *
     * @param question
     * @return All answers present in db
     */
    public List<AnswerEntity> getAllAnswersToQuestion(final QuestionEntity question) {
        log.info("get all answers to the question from the database");
        return entityManager.createNamedQuery("getAllAnswersToQuestion", AnswerEntity.class).setParameter("question",question).getResultList();
    }
}
