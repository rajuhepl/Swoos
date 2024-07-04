package com.example.swoos.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

    @GetMapping("/")
    public String handleApiRequest() {
        return "it's working!!!......";
    }
}