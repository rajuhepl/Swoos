package com.example.swoos.service;

import com.example.swoos.exception.CustomValidationException;
import com.example.swoos.model.DropDownModel;

import java.util.List;

public interface DropDownService {
    List<DropDownModel> getAllIssues();

    DropDownModel createIssue(DropDownModel issue) throws CustomValidationException;

    void deleteDropDownById(Long id);
}
