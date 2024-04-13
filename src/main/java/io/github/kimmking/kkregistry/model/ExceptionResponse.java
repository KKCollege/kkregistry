package io.github.kimmking.kkregistry.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Description for this class.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/4/14 03:36
 */

@Data
@AllArgsConstructor
public class ExceptionResponse {
    private String errCode;
    private String errMessage;
}
