package com.example.swoos.controller;

import com.example.swoos.exception.CustomValidationException;
import com.example.swoos.model.Reason;
import com.example.swoos.service.ReasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
//@RequestMapping("/reasons")
public class ReasonController {
    private final ReasonService reasonService;

    @Autowired
    public ReasonController(ReasonService reasonService) {
        this.reasonService = reasonService;
    }

    @PostMapping("/add")
    public ResponseEntity<String> addReason(@RequestParam int rowId, @RequestParam String reason) throws CustomValidationException {
       return ResponseEntity.ok(reasonService.addReason(rowId, reason));
    }

    @GetMapping("/last")
    public ResponseEntity<Reason> getLastReason(@RequestParam int rowId) {
        return ResponseEntity.ok(reasonService.getLastReason(rowId));
    }

    @GetMapping("/previous")
    public ResponseEntity<Reason> getPreviousReason(@RequestParam int rowId) {
        return ResponseEntity.ok(reasonService.getPreviousReason(rowId));
    }
}



