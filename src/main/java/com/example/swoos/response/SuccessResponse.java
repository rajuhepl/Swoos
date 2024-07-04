package com.example.swoos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuccessResponse<T> {

    private int statusCode = 200;
    private String statusMessage = "Success";
    private T data ;

}

