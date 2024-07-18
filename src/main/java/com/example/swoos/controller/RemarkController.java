package com.example.swoos.controller;

import com.example.swoos.model.Remarks;
import com.example.swoos.service.RemarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@RequestMapping("/api")
public class RemarkController {

    @Autowired
    private RemarkService remarkService;

    @PostMapping("/remarks")
    public ResponseEntity<Remarks> createRemark(@RequestBody Remarks remarks) {
        return ResponseEntity.ok(remarkService.createRemark(remarks));
    }
}
