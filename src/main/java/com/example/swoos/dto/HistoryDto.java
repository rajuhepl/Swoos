package com.example.swoos.dto;

import com.example.swoos.model.MergedModel;
import lombok.Data;

import java.util.List;

@Data
public class HistoryDto {
    private List<MergedModelDto> history;
    private String downloadLink;
}
