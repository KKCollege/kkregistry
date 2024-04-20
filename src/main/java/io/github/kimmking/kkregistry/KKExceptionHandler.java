package io.github.kimmking.kkregistry;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * uni-exception handler.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/4/20 下午8:29
 */

@RestControllerAdvice
public class KKExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handleException(Exception e) {
        return new ExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
}
