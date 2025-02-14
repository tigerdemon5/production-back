package com.boot.swlugweb.v1.password;

import com.boot.swlugweb.v1.signup.SignupUserInfoDomain;
import com.boot.swlugweb.v1.signup.SignupUsersDomain;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//비밀번호 수정
@Service
public class PasswordService {
    private final PasswordRepository passwordRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordService(PasswordRepository passwordRepository,
                           PasswordEncoder passwordEncoder) {
        this.passwordRepository = passwordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean verifyUserIdAndEmail(String userId, String email) {
        return passwordRepository.findByUserIdAndEmail(userId, email).isPresent();
    }

    @Transactional
    public void resetPassword(String userId, String newPassword) {
        SignupUserInfoDomain userInfo = passwordRepository.findAll().stream()
                .filter(info -> userId.equals(info.getSignupUsers().getUserId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        SignupUsersDomain user = userInfo.getSignupUsers();
        user.setPw(passwordEncoder.encode(newPassword));
    }
}