package com.boot.swlugweb.v1.password;

import com.boot.swlugweb.v1.email.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
//비밀번호 수정
@RestController
@RequestMapping("/api/password")
public class PasswordController {
    private final PasswordService passwordService;
    private final EmailService emailService;

    public PasswordController(PasswordService passwordService, EmailService emailService) {
        this.passwordService = passwordService;
        this.emailService = emailService;
    }

    // 1단계: 아이디와 이메일 검증 및 인증메일 발송
    @PostMapping("/verify")
    public ResponseEntity<?> verifyUserAndSendEmail(@RequestBody @Valid PasswordRequestDto requestDto) {
        try {
            if (!passwordService.verifyUserIdAndEmail(requestDto.getUserId(), requestDto.getEmail())) {
                return ResponseEntity.badRequest().body("이메일이 일치하지 않습니다.");
            }

            emailService.joinEmail(requestDto.getEmail());
            return ResponseEntity.ok("인증번호가 발송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("인증메일 발송에 실패했습니다: " + e.getMessage());
        }
    }

    // 2단계: 인증번호 검증
    @PostMapping("/verify-auth")
    public ResponseEntity<?> verifyAuthNumber(@RequestBody @Valid PasswordVerifyDto verifyDto) {
        try {
            if (!passwordService.verifyUserIdAndEmail(verifyDto.getUserId(), verifyDto.getEmail())) {
                return ResponseEntity.badRequest().body("아이디와 이메일이 일치하지 않습니다.");
            }

            if (!emailService.checkAuthNumber(verifyDto.getEmail(), verifyDto.getAuthNumber())) {
                return ResponseEntity.badRequest().body("인증번호가 일치하지 않습니다.");
            }

            return ResponseEntity.ok("인증이 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("인증에 실패했습니다: " + e.getMessage());
        }
    }

    // 3단계: 비밀번호 재설정
    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid PasswordUpdateDto updateDto, HttpServletRequest request, HttpServletResponse response) {
        try {
            passwordService.resetPassword(updateDto.getUserId(), updateDto.getNewPassword());

            // 로그인 상태 확인
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                // 로그아웃 처리
                new SecurityContextLogoutHandler().logout(request, response, authentication);
                return ResponseEntity.ok("비밀번호가 성공적으로 재설정되었습니다. 로그인 화면으로 이동합니다.");
            }

            return ResponseEntity.ok("비밀번호가 성공적으로 재설정되었습니다. 로그인 화면으로 이동합니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("비밀번호 재설정에 실패했습니다: " + e.getMessage());
        }
    }
}
