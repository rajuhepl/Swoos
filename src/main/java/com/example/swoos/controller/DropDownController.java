package com.example.swoos.controller;

import com.example.swoos.exception.CustomValidationException;
import com.example.swoos.model.DropDownModel;
import com.example.swoos.service.DropDownService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DropDownController {
    @Autowired
    private DropDownService dropDownService;

    @GetMapping("/get")
    public ResponseEntity<List<DropDownModel>> getAllIssues() {
        return ResponseEntity.ok(dropDownService.getAllIssues());
    }

    @PostMapping("/post")
    public ResponseEntity<DropDownModel> createIssue(@RequestBody DropDownModel issue) throws CustomValidationException {
        return ResponseEntity.ok(dropDownService.createIssue(issue));
    }

    @PostMapping("/reason/{id}")
    public ResponseEntity<String> deleteDropDown(@PathVariable Long id) {
        try {
            dropDownService.deleteDropDownById(id);
            return ResponseEntity.ok("Dropdown deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}


