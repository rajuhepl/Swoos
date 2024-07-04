package com.example.swoos.service;

import com.example.swoos.dto.MergeRequestDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MergeFileService {
    ResponseEntity<String> readCSVData(MultipartFile csvFile, MultipartFile excelFile, HttpServletResponse response);

    String update(List<MergeRequestDTO> mergeRequestDTO);
}
