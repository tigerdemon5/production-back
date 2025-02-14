package com.boot.swlugweb.v1.password;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
//비밀번호 수정
@Getter
@Setter
public class PasswordVerifyDto {
    @NotEmpty(message = "아이디를 입력해주세요.")
    private String userId;

    @NotEmpty(message = "이메일을 입력해주세요.")
    private String email;

    @NotNull(message = "인증번호를 입력해주세요.")
    private Integer authNumber;
}