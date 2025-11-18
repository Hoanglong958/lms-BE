package com.ra.base_spring_boot.exception;

public class HttpForbiden extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public HttpForbiden(String message)
    {
        super(message);
    }
}
