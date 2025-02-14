package com.boot.swlugweb.v1.login;

public class LoginResponseDto {
    private final boolean success;
    private final String message;
    private final String userId;
    private final String role;

    public LoginResponseDto(boolean success, String message, String userId, String role) {
        this.success = success;
        this.message = message;
        this.userId = userId;
        this.role = role;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }
}