package com.example.swoos.service;

import com.example.swoos.exception.CustomValidationException;
import com.example.swoos.model.Reason;

public interface ReasonService {
    String addReason(long rowId, String reason) throws CustomValidationException;

    Reason getLastReason(int rowId);

    Reason getPreviousReason(int rowId);
}
