package com.ra.base_spring_boot.exception;

public class HttpNotFound extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public HttpNotFound(String message)
    {
        super(message);
    }
}
