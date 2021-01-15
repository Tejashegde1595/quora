
package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.QuestionDetailsResponse;
import com.upgrad.quora.api.model.QuestionRequest;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import com.upgrad.quora.api.model.QuestionResponse;
import org.springframework.web.bind.annotation.RequestMethod;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/question")
public class QuestionController {
    @Autowired
    private QuestionService questionService;

    @RequestMapping(method = RequestMethod.POST,path = "/create",produces = MediaType.APPLICATION_JSON_UTF8_VALUE,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> createQuestion(final QuestionRequest questionRequest,@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {
        QuestionEntity questionEntity = new QuestionEntity();
        questionEntity.setContent(questionRequest.getContent());
        questionEntity.setUuid(UUID.randomUUID().toString());
        questionEntity.setDate(ZonedDateTime.now());

        final QuestionEntity createdQuestionEntity = questionService.createQuestion(questionEntity,authorization);

        final QuestionResponse questionResponse = new QuestionResponse().id(createdQuestionEntity.getUuid()).status("QUESTION CREATED");

        return new ResponseEntity<QuestionResponse>(questionResponse, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET,path = "/all",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getQuestions(@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException{
        final List<QuestionEntity> questionEntityList = questionService.getAllQuestions(authorization);
        List<QuestionDetailsResponse> questionDetailsResponsesList = new ArrayList<QuestionDetailsResponse>();
        Iterator<QuestionEntity> iterator =questionEntityList.listIterator();
        while(iterator.hasNext()){
            QuestionEntity questionEntity = iterator.next();
            QuestionDetailsResponse questionDetailsResponse = new QuestionDetailsResponse().id(questionEntity.getUuid()).content(questionEntity.getContent());
            questionDetailsResponsesList.add(questionDetailsResponse);
        }
        return new ResponseEntity<List<QuestionDetailsResponse>>(questionDetailsResponsesList,HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET,path = "/all/{userId}",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getQuestionsByUser(@PathVariable("userId") final String userId,@RequestHeader("authorization") final String authorization ) throws AuthorizationFailedException, UserNotFoundException {
        final List<QuestionEntity> questionEntityList = questionService.getQuestionsByUser(userId, authorization);
        List<QuestionDetailsResponse> questionDetailsResponsesList = new ArrayList<QuestionDetailsResponse>();
        Iterator<QuestionEntity> iterator =questionEntityList.listIterator();
        while(iterator.hasNext()){
            QuestionEntity questionEntity = iterator.next();
            QuestionDetailsResponse questionDetailsResponse = new QuestionDetailsResponse().id(questionEntity.getUuid()).content(questionEntity.getContent());
            questionDetailsResponsesList.add(questionDetailsResponse);
        }
        return new ResponseEntity<List<QuestionDetailsResponse>>(questionDetailsResponsesList,HttpStatus.OK);
    }
}