package com.example.swoos.service;

import com.example.swoos.exception.CustomValidationException;
import com.example.swoos.model.MergedModel;
import com.example.swoos.response.SuccessResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface ExcelService {
    SuccessResponse<Object> historyToExcel(HttpServletResponse response) throws CustomValidationException;
}
