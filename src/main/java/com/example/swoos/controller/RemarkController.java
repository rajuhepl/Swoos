package com.example.swoos.controller;

import com.example.swoos.model.Remarks;
import com.example.swoos.service.RemarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@RequestMapping("/api")
public class RemarkController {

    @Autowired
    RemarkService remarkService;

    @PostMapping("/remarks")
    public Remarks createRemark(@RequestBody Remarks remarks) {
        return remarkService.createRemark(remarks);
    }
}
