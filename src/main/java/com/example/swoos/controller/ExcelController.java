package com.example.swoos.controller;

import com.example.swoos.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
public class ExcelController {
    @Autowired
    ExcelService excelService ;


   @GetMapping("/historydownload")
   public ResponseEntity<InputStreamResource> exportHistoryToExcel(@RequestParam boolean historyFlag) {
       ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
       try {
           return excelService.historyToExcel(outputStream, historyFlag);
       } catch (IOException e) {
           throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to export historyFlag to Excel", e);
       }
   }
    }

