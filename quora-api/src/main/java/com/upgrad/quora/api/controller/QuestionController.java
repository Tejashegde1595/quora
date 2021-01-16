
package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @Autowired
    private ModelMapper modelMapper;

    @RequestMapping(method = RequestMethod.POST,path = "/create",produces = MediaType.APPLICATION_JSON_UTF8_VALUE,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> createQuestion(final QuestionRequest questionRequest,@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {
        final QuestionEntity questionEntity = convertToQuestionEntity(questionRequest);
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

    @RequestMapping(method = RequestMethod.DELETE,path = "/delete/{questionId}",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionDeleteResponse> deleteQuestionById(@PathVariable("questionId") final String questionId,@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        QuestionEntity questionEntity = questionService.deleteQuestionById(questionId,authorization);
        QuestionDeleteResponse deleteResponse = new QuestionDeleteResponse().id(questionEntity.getUuid()).status("QUESTION DELETED");
        return new ResponseEntity<QuestionDeleteResponse>(deleteResponse,HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT,path = "/edit/{questionId}",produces = MediaType.APPLICATION_JSON_UTF8_VALUE,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionEditResponse> editQuestionById(final QuestionEditRequest questionEditRequest,@PathVariable("questionId") final String questionId,@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, InvalidQuestionException{
        QuestionEntity questionEntity = convertToQuestionEntity(questionEditRequest);
        QuestionEntity cretedQuestionEntity=questionService.editQuestion(questionEntity,questionId,authorization);
        QuestionEditResponse questionEditResponse = new QuestionEditResponse().id(cretedQuestionEntity.getUuid()).status("QUESTION EDITED");
        return new ResponseEntity<QuestionEditResponse>(questionEditResponse,HttpStatus.OK);
    }


    private QuestionEntity convertToQuestionEntity(final QuestionRequest questionRequest){
        QuestionEntity questionEntity= modelMapper.map(questionRequest,QuestionEntity.class);
        questionEntity.setUuid(UUID.randomUUID().toString());
        questionEntity.setContent(questionRequest.getContent());
        questionEntity.setDate(ZonedDateTime.now());
        return questionEntity;

    }

    private QuestionEntity convertToQuestionEntity(final QuestionEditRequest questionEditRequest){
        QuestionEntity questionEntity= modelMapper.map(questionEditRequest,QuestionEntity.class);
        questionEntity.setUuid(UUID.randomUUID().toString());
        questionEntity.setContent(questionEditRequest.getContent());
        questionEntity.setDate(ZonedDateTime.now());
        return questionEntity;
    }
}