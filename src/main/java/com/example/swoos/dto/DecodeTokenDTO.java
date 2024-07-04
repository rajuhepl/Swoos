package com.example.swoos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DecodeTokenDTO {

    private String sub;
    private int iat;
    private int exp;
    private Long id;
    private String email;
    private String mobile;

}

