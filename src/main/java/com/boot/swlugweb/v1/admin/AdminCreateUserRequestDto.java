package com.boot.swlugweb.v1.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCreateUserRequestDto {

    private String userId;
    private String pw;
    private String nickname;
    private String email;
    private String phone;
    private Integer roleType;

}
