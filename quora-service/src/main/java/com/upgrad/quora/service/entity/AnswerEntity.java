package com.upgrad.quora.service.entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.ZonedDateTime;

@Entity
@Table(name = "answer")
@NamedQueries({
        @NamedQuery(name = "getAnswerById", query = "SELECT ans FROM AnswerEntity ans WHERE ans.uuid=:uuid"),
        @NamedQuery(name = "getAllAnswersToQuestion", query = "select ans from AnswerEntity ans"),
        @NamedQuery(name = "getQuestionById", query = "select ans from AnswerEntity ans WHERE ans.question=:question")
})
public class AnswerEntity {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "UUID")
    @Size(max = 200)
    @NotNull
    private String uuid;

    @Column(name = "ANS")
    @Size(max = 255)
    @NotNull
    private String answer;

    @Column(name = "DATE")
    @NotNull
    private ZonedDateTime date;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "QUESTION_ID")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private QuestionEntity question;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public void setDate(ZonedDateTime date) {
        this.date = date;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public QuestionEntity getQuestion() {
        return question;
    }

    public void setQuestion(QuestionEntity questionEntity) {
        this.question = questionEntity;
    }
}
