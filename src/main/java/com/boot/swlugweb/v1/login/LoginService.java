package com.boot.swlugweb.v1.login;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LoginService {
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_TIME_MINUTES = 10;

    private final LoginRepository loginRepository;
    private final PasswordEncoder passwordEncoder;
    private final HttpSession session;

    public LoginService(LoginRepository loginRepository,
                        PasswordEncoder passwordEncoder,
                        HttpSession session) {
        this.loginRepository = loginRepository;
        this.passwordEncoder = passwordEncoder;
        this.session = session;
    }

    public LoginResponseDto authenticateUser(String userId, String password) {
        Optional<LoginDomain> userOptional = loginRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return new LoginResponseDto(false, "아이디 또는 비밀번호가 올바르지 않습니다", null, null);
        }

        LoginDomain user = userOptional.get();

        // 계정 잠금 확인 및 해제 처리
        if (user.getAccountLocked() != null && user.getAccountLocked()) {
            if (user.getLastAttemptTime() != null &&
                    LocalDateTime.now().minusMinutes(LOCK_TIME_MINUTES).isAfter(user.getLastAttemptTime())) {
                // 잠금 시간이 지났으면 잠금 해제
                user.setAccountLocked(false);
                user.setLoginAttempts(0);
                loginRepository.save(user);
            } else {
                // 아직 잠금 시간이 지나지 않음
                return new LoginResponseDto(false,
                        "계정이 잠겼습니다. " + LOCK_TIME_MINUTES + "분 후에 다시 시도해주세요", null, null);
            }
        }

        // 비밀번호 확인
        if (passwordEncoder.matches(password, user.getPw())) {
            // 로그인 성공
            user.setLoginAttempts(0);
            user.setLastAttemptTime(LocalDateTime.now());
            loginRepository.save(user);

            session.setAttribute("USER", userId);
            return new LoginResponseDto(true, "로그인 성공", userId, null);
        } else {
            // 로그인 실패
            int attempts = user.getLoginAttempts() == null ? 0 : user.getLoginAttempts();
            user.setLoginAttempts(attempts + 1);
            user.setLastAttemptTime(LocalDateTime.now());

            // 최대 시도 횟수 초과 시 계정 잠금
            if (user.getLoginAttempts() >= MAX_LOGIN_ATTEMPTS) {
                user.setAccountLocked(true);
                loginRepository.save(user);
                return new LoginResponseDto(false,
                        "로그인 시도 횟수를 초과하여 계정이 잠겼습니다. " +
                                LOCK_TIME_MINUTES + "분 후에 다시 시도해주세요", null, null);
            }

            loginRepository.save(user);
            return new LoginResponseDto(false,
                    "아이디 또는 비밀번호가 올바르지 않습니다. 남은 시도 횟수: " +
                            (MAX_LOGIN_ATTEMPTS - user.getLoginAttempts()) + "회", null, null);
        }
    }

    public void logout() {
        session.invalidate();
    }

    public String getCurrentUser() {
        return (String) session.getAttribute("USER");
    }

    public boolean isLoggedIn() {
        return session.getAttribute("USER") != null;
    }
}