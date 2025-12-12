package com.ra.base_spring_boot.payload.response;

import lombok.Builder;

@Builder
public class ResponseWrapper {
    private int status;
    private String message;
    private Object data;
}
