package com.boot.swlugweb.v1.login;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
//로그인 수정
@RestController
@RequestMapping("/api/login")  //수정
public class LoginController {
    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    // 로그인 페이지 GET 요청 처리 (front 페이지와 연결해줘야 함)
    @GetMapping
    public ResponseEntity<String> loginPage() {
        return ResponseEntity.ok("Login page");
    }

    // 로그인 처리 POST 요청
//    @PostMapping
//    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
//        LoginResponseDto response = loginService.authenticateUser(
//                loginRequestDto.getUserId(),
//                loginRequestDto.getPassword()
//        );
//
//        if (response.isSuccess()) {
//            return ResponseEntity.ok(response);
//        } else {
//            return ResponseEntity.status(401).body(response);
//        }
//    }

    // 로그아웃 처리
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        loginService.logout();
        return ResponseEntity.ok("Logged out successfully");
    }

    // 로그인 상태 확인 (테스트 목적- 임시로 구현)
    @GetMapping("/check")
    public ResponseEntity<?> checkLoginStatus() {
        if (loginService.isLoggedIn()) {
            return ResponseEntity.ok(Map.of(
                    "loggedIn", true,
                    "userId", loginService.getCurrentUser()
            ));
        }
        return ResponseEntity.ok(Map.of("loggedIn", false));
    }
}