package com.example.swoos.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProductDto {
    private Long id;
    private String productName;
    private String channel;
    private LocalDate date;
    private String platform;
}
