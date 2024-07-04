package com.example.swoos.service;

import com.example.swoos.model.Reason;

public interface ReasonService {
    void addReason(int rowId, String reason);

    Reason getLastReason(int rowId);

    Reason getPreviousReason(int rowId);
}
