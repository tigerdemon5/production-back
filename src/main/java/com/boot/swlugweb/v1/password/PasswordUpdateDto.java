package com.boot.swlugweb.v1.password;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
//비밀번호 수정
@Getter
@Setter
public class PasswordUpdateDto {
    @NotEmpty(message = "아이디를 입력해주세요.")
    private String userId;

    @NotEmpty(message = "새로운 비밀번호를 입력해주세요.")
    private String newPassword;
}