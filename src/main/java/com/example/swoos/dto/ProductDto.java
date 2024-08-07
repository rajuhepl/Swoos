package com.example.swoos.dto;

import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProductDto {
    private Long id;
    private String productName;
    private String channel;
    private String date;
    private String platform;
}
