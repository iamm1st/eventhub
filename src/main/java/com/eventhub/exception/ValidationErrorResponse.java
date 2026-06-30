package com.eventhub.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class ValidationErrorResponse {

    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> errors;
    private LocalDateTime timestamp;
}