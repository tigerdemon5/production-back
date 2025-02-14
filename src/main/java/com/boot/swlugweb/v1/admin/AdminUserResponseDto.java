package com.boot.swlugweb.v1.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserResponseDto {
    private Integer userInfoNum;
    private String userId;
    private String nickname;
    private Integer roleType;

    public AdminUserResponseDto(Integer userInfoNum, String userId, String nickname, Integer roleType) {
        this.userInfoNum = userInfoNum;
        this.userId = userId;
        this.nickname = nickname;
        this.roleType = roleType;
    }
}
