package com.example.swoos.dto;

import lombok.Data;

@Data
public class MergeRequestDTO {

    private Long mergedId;
    private String reason;
    private String remarks;
}
