package com.boot.swlugweb.v1.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserUpdateRequestDto {
    private Integer userInfoNum;
    private String userId;
    private String nickname;
    private String email;
    private String phone;
    private Integer roleType;
}
