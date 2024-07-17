package com.example.swoos.controller;

import com.example.swoos.exception.CustomValidationException;
import com.example.swoos.model.Reason;
import com.example.swoos.service.ReasonService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public String addReason(@RequestParam int rowId, @RequestParam String reason) throws CustomValidationException {
       return reasonService.addReason(rowId, reason);
    }

    @GetMapping("/last")
    public Reason getLastReason(@RequestParam int rowId) {
        return reasonService.getLastReason(rowId);
    }

    @GetMapping("/previous")
    public Reason getPreviousReason(@RequestParam int rowId) {
        return reasonService.getPreviousReason(rowId);
    }
}



