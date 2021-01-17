package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.AnswerBusinessService;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class AnswerController {

    @Autowired
    private AnswerBusinessService answerBusinessService;

    @Autowired
    private ModelMapper modelMapper;



    /**
     * Create answer for a question
     *
     * @param answerRequest
     * @param questionId
     * @param authorization
     * @return questionId for which answer is created
     * @throws InvalidQuestionException
     * @throws UserNotFoundException
     */
    @RequestMapping(method = RequestMethod.POST, path = "/question/{questionId}/answer/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerResponse> createAnswer(final AnswerRequest answerRequest, @PathVariable("questionId") final String questionId, @RequestHeader("authorization") final String authorization) throws InvalidQuestionException, AuthorizationFailedException {
        final AnswerEntity answerEntity = convertToAnswerEntity(answerRequest);
        AnswerEntity createdAnswerEntity = answerBusinessService.createAnswer(answerEntity, authorization, questionId);
        AnswerResponse answerResponse = new AnswerResponse().id(createdAnswerEntity.getUuid()).status("ANSWER CREATED");
        return new ResponseEntity<AnswerResponse>(answerResponse, HttpStatus.CREATED);
    }

    /**
     * Edit an existing answer
     *
     * @param answerEditRequest
     * @param answerId
     * @param authorization
     * @return answerId of the Answer edited
     * @throws AuthorizationFailedException
     * @throws AnswerNotFoundException
     */
    @RequestMapping(method = RequestMethod.PUT, path = "/answer/edit/{answerId}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerEditResponse> editAnswerContent(final AnswerEditRequest answerEditRequest, @PathVariable("answerId") final String answerId, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, AnswerNotFoundException {
        AnswerEntity answerEntity = convertEditAnswerEntity(answerEditRequest);
        AnswerEntity updateAnswerEntity = answerBusinessService.editAnswer(authorization, answerId, answerEntity);
        AnswerEditResponse answerEditResponse = new AnswerEditResponse().id(answerEntity.getUuid()).status("ANSWER EDITED");
        return new ResponseEntity<AnswerEditResponse>(answerEditResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/answer/delete/{answerId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerDeleteResponse> deleteAnswer(@PathVariable("answerId") final String answerId, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, AnswerNotFoundException {
        AnswerEntity answerEntity = answerBusinessService.deleteAnswer(answerId, authorization);
        AnswerDeleteResponse answerDeleteResponse = new AnswerDeleteResponse().id(answerEntity.getUuid()).status("ANSWER DELETED");
        return new  ResponseEntity<AnswerDeleteResponse>(answerDeleteResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/answer/all/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<AnswerDetailsResponse>> getAllAnswersToQuestion(@PathVariable("questionId") String questionId, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        List<AnswerEntity> answerEntityList = answerBusinessService.getAllAnswersToQuestion(questionId, authorization);
        List<AnswerDetailsResponse> answerDetailsResponsesList = new ArrayList<AnswerDetailsResponse>();
        Iterator<AnswerEntity> iterator = answerEntityList.iterator();

        while(iterator.hasNext()) {
            AnswerEntity answerEntity = iterator.next();
            AnswerDetailsResponse answerDetailsResponse = new AnswerDetailsResponse().id(answerEntity.getUuid()).answerContent(answerEntity.getAnswer());
            answerDetailsResponsesList.add(answerDetailsResponse);
        }
        return new ResponseEntity<List<AnswerDetailsResponse>>(answerDetailsResponsesList, HttpStatus.OK);
    }

    private AnswerEntity convertToAnswerEntity(final AnswerRequest answerRequest) {
        AnswerEntity answerEntity = modelMapper.map(answerRequest, AnswerEntity.class);
        answerEntity.setUuid(UUID.randomUUID().toString());
        answerEntity.setAnswer(answerRequest.getAnswer());
        answerEntity.setDate(ZonedDateTime.now());
        return answerEntity;
    }

    private AnswerEntity convertEditAnswerEntity(final AnswerEditRequest answerEditRequest) {
        AnswerEntity answerEntity = modelMapper.map(answerEditRequest, AnswerEntity.class);
        answerEntity.setUuid(UUID.randomUUID().toString());
        answerEntity.setAnswer(answerEditRequest.getContent());
        answerEntity.setDate(ZonedDateTime.now());
        return answerEntity;
    }
}
