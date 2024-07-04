//package com.example.swoos.Controller;
//
//import com.example.swoos.Request.LoginRequest;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api")
//class LoginController {
//
//    private final String hardcodedEmail = "swoos@gmail.com";
//    private final String hardcodedPassword = "123";
//
//    @PostMapping("/signin")
//    public String login(@RequestBody LoginRequest request) {
//        String email = request.getEmail();
//        String password = request.getPassword();
//
//        if (email.equals(hardcodedEmail) && password.equals(hardcodedPassword)) {
//            return "Login successful";
//        } else {
//            return "Login failed";
//        }
//    }
//}
//
