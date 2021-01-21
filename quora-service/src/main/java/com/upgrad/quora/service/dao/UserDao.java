package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class UserDao {


    @PersistenceContext
    private EntityManager entityManager;

    /** To persist user entity in db
     * @param userEntity
     * @return
     */
    public UserEntity createUser(UserEntity userEntity) {
        entityManager.persist(userEntity);
        return userEntity;
    }

    /** get an user from db based on user uuid
     * @param userUuid
     * @return
     */
    public UserEntity getUser(final String userUuid) {
        try {
            return entityManager.createNamedQuery("userByUuid", UserEntity.class).setParameter("uuid", userUuid)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /** get an user from db based on user email
     * @param email
     * @return
     */
    public UserEntity getUserByEmail(final String email) {
        try {
            return entityManager.createNamedQuery("userByEmail", UserEntity.class).setParameter("email", email).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /** get an user from db based on user name
     * @param UserName
     * @return
     */
    public UserEntity getUserByUserName(final String UserName) {
        try {
            return entityManager.createNamedQuery("userByUserName", UserEntity.class).setParameter("userName", UserName).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /** To persist user auth-token entity in db
     * @param userAuthTokenEntity
     * @return
     */
    public UserAuthTokenEntity createAuthToken(final UserAuthTokenEntity userAuthTokenEntity) {
        entityManager.persist(userAuthTokenEntity);
        return userAuthTokenEntity;
    }

    /** To update an already existing user auth-token entity in db
     * @param userAuthTokenEntity
     * @return
     */
    public UserAuthTokenEntity updateAuthToken(final UserAuthTokenEntity userAuthTokenEntity) {
        entityManager.merge(userAuthTokenEntity);
        return userAuthTokenEntity;
    }

    /** get user auth-token entity from db based on token string
     * @param accessToken
     * @return
     */
    public UserAuthTokenEntity getUserAuthToken(final String accessToken) {
        try {
            return entityManager.createNamedQuery("userAuthTokenByAccessToken", UserAuthTokenEntity.class).setParameter("accessToken", accessToken).getSingleResult();
        } catch (NoResultException nre) {

            return null;
        }
    }

    /** to remove an user from the user table
     * @param userEntity
     */
    public void deleteUser(final UserEntity userEntity) {
        entityManager.remove(userEntity);
    }
}