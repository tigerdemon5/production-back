package com.boot.swlugweb.v1.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserInfoResponseDto {

    private Integer userInfoNum;
    private String userId;
    private String nickname;
    private String email;
    private String phone;
    private Integer role_type;
    private Integer version;

    public AdminUserInfoResponseDto(Integer userInfoNum, String userId, String nickname, String email, String phone, Integer role_type) {
        this.userId = userId;
        this.nickname = nickname;
        this.email = email;
        this.phone = phone;
        this.role_type = role_type;
    }
}
