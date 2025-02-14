package com.boot.swlugweb.v1.signup;

import lombok.Getter;
import lombok.Setter;
//회원가입
@Getter
@Setter
public class SignupRequestDto {

    private String userId;
    private String pw;
    private String confirmPw;
    private String nickname;
    private String email;
    private String phone;
    private Integer ruleType; //유저 역할은 클라이언트에서 X 따로 객체 저장 시에 따로 지정 (1로)
}
