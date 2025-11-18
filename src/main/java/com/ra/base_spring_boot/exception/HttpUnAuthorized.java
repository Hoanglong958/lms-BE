package com.ra.base_spring_boot.exception;

public class HttpUnAuthorized extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public HttpUnAuthorized(String message)
    {
        super(message);
    }
}
