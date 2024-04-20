package io.github.kimmking.kkregistry;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * wrap exception response.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/4/20 下午8:31
 */
@AllArgsConstructor
@Data
public class ExceptionResponse {
    private HttpStatus httpStatus;
    private String message;
}
