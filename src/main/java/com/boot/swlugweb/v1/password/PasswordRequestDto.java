package com.boot.swlugweb.v1.password;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
//비밀번호 수정
@Getter
@Setter
public class PasswordRequestDto {
    @NotEmpty(message = "아이디를 입력해주세요.")
    private String userId;

    @Email
    @NotEmpty(message = "이메일을 입력해주세요.")
    private String email;
}