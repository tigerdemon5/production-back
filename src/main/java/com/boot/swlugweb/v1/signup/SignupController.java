package com.boot.swlugweb.v1.signup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
//회원가입
@RestController
@RequestMapping("/api/signup")
public class SignupController {

    @Autowired
    private SignupService signupService;

    @PostMapping("/check-id")
    public ResponseEntity<String> checkDuplicateId(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        try {
            boolean isDuplicate = signupService.existsById(userId);
            return ResponseEntity.ok(isDuplicate ? "duplicate" : "available");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("error");
        }
    }

    @PostMapping("")
    public String register(@RequestBody SignupRequestDto signuprequestdto) {
        System.out.println("User ID: " + signuprequestdto.getUserId());
        System.out.println("Email: " + signuprequestdto.getEmail());
        System.out.println("Phone: " + signuprequestdto.getPhone());
        signupService.registerUser(signuprequestdto);
        return "success";
    }
}