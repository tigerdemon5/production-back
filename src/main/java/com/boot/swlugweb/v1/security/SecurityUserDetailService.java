package com.boot.swlugweb.v1.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class SecurityUserDetailService implements UserDetailsService {

    private final SecurityUserInfoRepository securityUserInfoRepository;
    // Logger 생성
    private static final Logger logger = LoggerFactory.getLogger(SecurityUserDetailService.class);

    public SecurityUserDetailService(SecurityUserInfoRepository securityUserInfoRepository) {
        this.securityUserInfoRepository = securityUserInfoRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //user_id로 사용자 정보 조회
        System.out.println("Received login request with username: " + username); //username 디버깅

        logger.debug("Debugging login request for: {}", username);

        //unsername을 사용해서 사용자의 ruletype을 찾음
        SecurityUserInfoDomain userInfoDomain = securityUserInfoRepository.findBySecurityUsers_userId(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with user_id: " + username));

        //user_id와 pw가 null일 경우 예외 처리
        if (userInfoDomain.getSecurityUsers().getUserId() == null || userInfoDomain.getSecurityUsers().getPw() == null) {
            throw new UsernameNotFoundException("Invalid user data: user_id or password is null");
        }

        //권한 생성
        String role = mapRoleTypeToString(userInfoDomain.getSecurityUserRuleType().getRole_type());
        //System.out.println("Role: " + role);

        logger.debug("User {} has role: {}", username, role);

        return new org.springframework.security.core.userdetails.User(
                userInfoDomain.getSecurityUsers().getUserId(),
                userInfoDomain.getSecurityUsers().getPw(),
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }

    private String mapRoleTypeToString(int roleType) {
        switch (roleType) {
            case 0: return "ROLE_ADMIN"; //hasAnyRole이 ROLE을 앞에 붙여서 비교 -> 앞에 ROLE 붙여줘야 함 ;;;
            case 1: return "ROLE_USER";
            case 2: return "ROLE_GUEST";
            default: throw new IllegalArgumentException("Invalid rule type: " + roleType);
        }
    }


}
