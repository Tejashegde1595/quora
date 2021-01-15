package com.upgrad.quora.service.common;

public class Constants {
    public static final String SECRET = "SecretKeyToGenJWTs";
    public static final long EXPIRATION_TIME = 8;
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Basic ";
    public static final String TOKEN_ISSUER = "https://quora.io";
    public static final String LOGIN_MESSAGE = "SIGNED IN SUCCESSFULLY";
    public static final String LOGOUT_MESSAGE = "SIGNED OUT SUCCESSFULLY";
    public static final String USER_REGISTRATION_MESSAGE = "USER SUCCESSFULLY REGISTERED";
    public static final String DELETE_USER_MESSAGE = "USER SUCCESSFULLY DELETED";
}