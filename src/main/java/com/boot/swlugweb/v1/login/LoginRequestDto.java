package com.boot.swlugweb.v1.login;
//로그인수정
//userId와 password
public class LoginRequestDto {
    private String userId;
    private String password;

    public String getUserId() {
        return userId;
    }
//    public void setUserId(String userId) {
//        this.userId = userId;
//    }
    public String getPassword() {
        return password;
    }
//    public void setPassword(String password) {
//        this.password = password;
//    }
}