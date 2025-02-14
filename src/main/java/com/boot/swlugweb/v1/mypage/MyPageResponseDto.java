package com.boot.swlugweb.v1.mypage;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class MyPageResponseDto {
    private String userId;
    private String nickname;
    private String phone;
    private String email;
    @Getter
    @Setter
    private List<MyPageBlogDomain> blogInfo;

    public MyPageResponseDto(String userId, String nickname, String phone, String email) {
        this.userId = userId;
        this.nickname = nickname;
        this.phone = phone;
        this.email = email;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getNickname() { return nickname; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }

}