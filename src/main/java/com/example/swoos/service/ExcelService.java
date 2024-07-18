package com.example.swoos.service;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public interface ExcelService {
    ResponseEntity<InputStreamResource> historyToExcel(ByteArrayOutputStream outputStream, boolean history) throws IOException;

}
