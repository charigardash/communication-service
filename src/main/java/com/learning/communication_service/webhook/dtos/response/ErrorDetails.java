package com.learning.communication_service.webhook.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class ErrorDetails<T> {

    private LocalDateTime timestamp;

    private T exception;
}
