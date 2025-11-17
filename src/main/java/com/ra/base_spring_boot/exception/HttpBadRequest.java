package com.ra.base_spring_boot.exception;

public class HttpBadRequest extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public HttpBadRequest(String message)
    {
        super(message);
    }
}
