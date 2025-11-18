package com.ra.base_spring_boot.exception;

public class HttpConflict extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public HttpConflict(String message)
    {
        super(message);
    }
}
