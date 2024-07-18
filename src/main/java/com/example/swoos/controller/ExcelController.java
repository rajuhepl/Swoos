package com.example.swoos.controller;

import com.example.swoos.exception.CustomValidationException;
import com.example.swoos.service.ExcelService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExcelController {
    @Autowired
    private ExcelService excelService ;
    @GetMapping("/historydownload")
    public ResponseEntity<String> exportDataToExcel(HttpServletResponse response) throws CustomValidationException {
            excelService .historyToExcel(response);
        return ResponseEntity.ok("Excel file exported successfully");
    }
}
