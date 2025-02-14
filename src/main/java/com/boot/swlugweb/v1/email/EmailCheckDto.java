package com.boot.swlugweb.v1.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
//이메일 수정
@Getter
@Setter
public class EmailCheckDto {

    @Email
    @NotEmpty(message = "이메일을 입력해주세요.")
    private String email;

    @NotNull(message = "인증번호를 입력해주세요.")
    private int authNum;
}

