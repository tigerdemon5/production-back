package com.boot.swlugweb.v1.email;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
//이메일 수정
@RestController
@RequestMapping("/api/email")  // 추가
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/mailSend")
    public String mailSend(@RequestBody @Valid EmailRequestDto emailDto) {
        System.out.println("이메일 인증 메일 주소:" + emailDto.getEmail());
        return emailService.joinEmail(emailDto.getEmail());
    }

    @PostMapping("/mailAuthCheck")
    public String mailAuthCheck(@RequestBody @Valid EmailCheckDto emailCheckDto) {
        Boolean AuthCheck = emailService.checkAuthNumber(emailCheckDto.getEmail(), emailCheckDto.getAuthNum());

        if (AuthCheck) {
            return "ok";
        } else {
            return "fail";
        }
    }
}