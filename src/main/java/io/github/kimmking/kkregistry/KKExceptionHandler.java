package io.github.kimmking.kkregistry;

import io.github.kimmking.kkregistry.model.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Description for this class.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/4/14 03:32
 */

@RestControllerAdvice
public class KKExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse sendErrorResponse(Exception exception){
        return new ExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR.name(), exception.getMessage());
    }

}
