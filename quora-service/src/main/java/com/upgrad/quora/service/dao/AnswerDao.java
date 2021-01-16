package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.AnswerEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class AnswerDao {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Function to persist answer into db
     *
     * @param answerEntity
     * @return answer inserted in db
     */
    public AnswerEntity createAnswer(AnswerEntity answerEntity) {
        entityManager.persist(answerEntity);
        return answerEntity;
    }

    /**
     * Method to fetch answer from db on basis of answerId
     *
     * @param answerId
     * @return answer present in db
     */
    public AnswerEntity getAnswerById(String answerId) {
        try {
            return entityManager.createNamedQuery("getAnswerById", AnswerEntity.class).setParameter("uuid", answerId).getSingleResult();
        }
        catch (NoResultException nre) {
            return null;
        }
    }

    /**
     * Method to update and existing answer in db
     *
     * @param answerEntity
     */
    public void updateAnswer(AnswerEntity answerEntity) {
        entityManager.merge(answerEntity);
    }
}
