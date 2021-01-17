package com.upgrad.quora.service.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.ZonedDateTime;

@Entity
@Table(name = "answer")
@NamedQueries({
        @NamedQuery(name = "getAnswerById", query = "SELECT ans FROM AnswerEntity ans WHERE uuid=:uuid"),
        @NamedQuery(name = "getAllAnswersToQuestion", query = "select ans from AnswerEntity ans"),
        @NamedQuery(name = "getQuestionById", query = "select ans from AnswerEntity ans WHERE question_id=:question_id")
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

    @Column(name = "ANSWER")
    @Size(max = 255)
    @NotNull
    private String answer;

    @Column(name = "DATE")
    private ZonedDateTime date;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private UserEntity user_id;

    @ManyToOne
    @JoinColumn(name = "QUESTION_ID")
    private QuestionEntity question_id;

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

    public UserEntity getUser_Id() {
        return user_id;
    }

    public void setUser_Id(UserEntity user) {
        this.user_id = user_id;
    }

    public QuestionEntity getQuestion_Id() {
        return question_id;
    }

    public void setQuestion_Id(QuestionEntity questionEntity) {
        this.question_id = questionEntity;
    }
}
